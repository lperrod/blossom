package com.blossomproject.generator.configuration.model;

import jakarta.persistence.TemporalType;

public interface TemporalField extends Field {

  TemporalType getTemporalType();

}
