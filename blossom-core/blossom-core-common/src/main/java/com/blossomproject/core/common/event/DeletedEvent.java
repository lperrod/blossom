package com.blossomproject.core.common.event;

import com.blossomproject.core.common.dto.AbstractDTO;

/**
 * Application Event which notify the application when an entity has been deleted
 *
 * @param <DTO> the deleted {@code AbstractDTO}
 * @author Maël Gargadennnec
 */
public class DeletedEvent<DTO extends AbstractDTO> extends Event<DTO> {

  public DeletedEvent(Object source, DTO entity) {
    super(source, entity);
  }

}
