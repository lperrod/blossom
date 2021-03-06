package com.blossomproject.core.common.event;


import com.blossomproject.core.common.dto.AbstractDTO;

/**
 * Application Event which notify the application when an entity has been created
 *
 * @param <DTO> the created {@code AbstractDTO}
 * @author Maël Gargadennnec
 */
public class CreatedEvent<DTO extends AbstractDTO> extends Event<DTO> {

  public CreatedEvent(Object source, DTO dto) {
    super(source, dto);
  }
}
