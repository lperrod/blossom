package com.blossomproject.generator.classes;

import com.blossomproject.generator.configuration.model.Settings;
import com.blossomproject.generator.utils.GeneratorUtils;
import com.blossomproject.module.search.common.IndexationEngine;
import com.blossomproject.module.search.common.IndexationJob;
import com.helger.jcodemodel.JCodeModel;
import com.helger.jcodemodel.JDefinedClass;
import com.helger.jcodemodel.JFieldVar;
import com.helger.jcodemodel.JMethod;
import com.helger.jcodemodel.JMod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class IndexationJobGenerator implements ClassGenerator {

  @Override
  public void prepare(Settings settings, JCodeModel codeModel) {

  }

  @Override
  public JDefinedClass generate(Settings settings, JCodeModel codeModel) {
    try {
      JDefinedClass definedClass = codeModel._class(GeneratorUtils.getIndexationJobFullyQualifiedClassName(settings));
      definedClass._extends(codeModel.ref(IndexationJob.class));

      JFieldVar fileIndexationEngine = definedClass
        .field(JMod.PRIVATE, codeModel.ref(IndexationEngine.class)
          ,
          "fileIndexationEngine");
      fileIndexationEngine.annotate(Autowired.class);
      fileIndexationEngine.annotate(Qualifier.class).param("value",
        settings.getEntityNameLowerCamel() + "IndexationEngine");

      JMethod getIndexationEngine = definedClass.method(JMod.PROTECTED, IndexationEngine.class, "getIndexationEngine");
      getIndexationEngine.annotate(Override.class);
      getIndexationEngine.body()._return(fileIndexationEngine);

      return definedClass;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Can't generate indexation job class", e);
    }

  }
}
