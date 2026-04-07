package com.blossomproject.ui.theme;

import com.blossomproject.core.common.utils.misc.InMemoryURLFactory;
import de.larsgrefer.sass.embedded.SassCompiler;
import de.larsgrefer.sass.embedded.SassCompilerFactory;
import de.larsgrefer.sass.embedded.CompileSuccess;
import de.larsgrefer.sass.embedded.importer.CustomUrlImporter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.plugin.core.PluginRegistry;

public class ThemeCompilerImpl implements ThemeCompiler, CommandLineRunner, Ordered {

  private final static String[] FILENAMES = new String[]{"style", "style_mail"};

  private final PluginRegistry<Theme, String> registry;
  private final ResourceLoader resourceLoader;
  private final ConcurrentHashMap<String, String> cache;

  public ThemeCompilerImpl(
    PluginRegistry<Theme, String> registry,
    ResourceLoader resourceLoader) {
    this.registry = registry;
    this.resourceLoader = resourceLoader;
    this.cache = new ConcurrentHashMap<>();
  }

  @Override
  public void run(String... args) throws Exception {
    doCompileAll();
  }

  @Override
  public void doCompileAll() throws Exception {
    this.cache.clear();

    for (Theme theme : registry.getPlugins()) {
      for (String filename : FILENAMES) {

        final String scssPath = String.format("classpath:/scss/%s.scss", filename);
        final Resource scssResource = resourceLoader.getResource(scssPath);

        if (scssResource.exists()) {
          final String scssCode = IOUtils.toString(scssResource.getURL(), StandardCharsets.UTF_8);

          try (SassCompiler compiler = SassCompilerFactory.bundled()) {
            compiler.registerImporter(new BlossomScssImporter(theme));
            CompileSuccess output = compiler.compileScssString(scssCode);
            cache.put(theme.getName() + "_" + filename, output.getCss());
          }
        }
      }
    }
  }

  @Override
  public void getCss(String theme, String filename, OutputStream os) throws IOException {
    if (this.cache.containsKey(theme + "_" + filename)) {
      os.write(this.cache.get(theme + "_" + filename).getBytes());
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  private class BlossomScssImporter extends CustomUrlImporter {

    private final Theme theme;

    public BlossomScssImporter(Theme theme) {
      this.theme = theme;
    }

    @Override
    public URL canonicalizeUrl(String url) throws Exception {
      String basename = url;
      if (basename.startsWith("/")) {
        basename = basename.substring(1);
      }
      // Strip any leading path components, keep just the filename
      int lastSlash = basename.lastIndexOf('/');
      if (lastSlash >= 0) {
        basename = basename.substring(lastSlash + 1);
      }

      return resolveResource(basename);
    }

    private URL resolveResource(String basename) throws IOException {
      for (String prefix : new String[]{"_", ""}) {
        for (String suffix : new String[]{".scss", ".sass", ".css", ""}) {
          final Resource resource = resourceLoader
            .getResource("classpath:/scss/" + prefix + basename + suffix);
          if (resource != null && resource.exists()) {

            if (basename.equals("variables")) {
              String content = IOUtils.toString(resource.getURL(), StandardCharsets.UTF_8);
              for (Entry<String, String> message : theme.getMessages().entrySet()) {
                content = content.replace("%" + message.getKey() + "%", message.getValue());
              }
              return InMemoryURLFactory.getInstance().build("variables", content);
            }
            if (basename.equals("custom")) {
              return InMemoryURLFactory.getInstance()
                .build("custom", theme.getMessages().get("additionnalScss"));
            }

            return resource.getURL();
          }
        }
      }

      return null;
    }
  }
}
