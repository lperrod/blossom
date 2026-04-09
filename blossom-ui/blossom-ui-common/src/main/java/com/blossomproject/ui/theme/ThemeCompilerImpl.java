package com.blossomproject.ui.theme;

import java.io.IOException;
import java.io.OutputStream;

/**
 * No-op theme compiler. SCSS compilation is no longer needed since the Angular UI
 * uses Angular Material for styling instead of the legacy Bootstrap/Inspinia theme.
 */
public class ThemeCompilerImpl implements ThemeCompiler {

  @Override
  public void doCompileAll() {
    // No-op: Angular UI handles its own styling via Angular Material
  }

  @Override
  public void getCss(String theme, String filename, OutputStream os) throws IOException {
    // No-op: Angular UI handles its own styling
  }
}
