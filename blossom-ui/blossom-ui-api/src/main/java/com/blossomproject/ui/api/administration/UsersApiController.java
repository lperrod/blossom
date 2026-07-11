package com.blossomproject.ui.api.administration;

import com.blossomproject.core.common.dto.AbstractDTO;
import com.blossomproject.core.user.UserCreateForm;
import com.blossomproject.core.user.UserDTO;
import com.blossomproject.core.user.UserService;
import com.blossomproject.core.user.UserUpdateForm;
import com.blossomproject.module.search.common.AbstractQueryBuilder;
import com.blossomproject.module.search.common.AbstractSearchRequestBuilder;
import com.blossomproject.module.search.common.AbstractSearchResponse;
import com.blossomproject.module.search.common.SearchEngine;
import com.blossomproject.ui.stereotype.BlossomApiController;
import com.google.common.base.Strings;
import java.util.Map;
import java.util.Optional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by Maël Gargadennnec on 04/05/2017.
 */
@BlossomApiController
@RequestMapping("/administration/users")
public class UsersApiController {

  private final UserService userService;

  private final SearchEngine<? extends AbstractQueryBuilder, ? extends AbstractSearchRequestBuilder, ? extends AbstractSearchResponse, UserDTO> searchEngine;

  public UsersApiController(UserService userService,
    SearchEngine<? extends AbstractQueryBuilder, ? extends AbstractSearchRequestBuilder, ? extends AbstractSearchResponse, UserDTO> searchEngine) {
    this.userService = userService;
    this.searchEngine = searchEngine;
  }

  @GetMapping
  @PreAuthorize("hasAuthority('administration:users:read')")
  public Page<UserDTO> list(
    @RequestParam(value = "q", required = false) String q,
    @PageableDefault(size = 25) Pageable pageable) {

    if (Strings.isNullOrEmpty(q)) {
      return this.userService.getAll(pageable);
    }
    return this.searchEngine.search(q, pageable).getPage();
  }

  @PostMapping
  @PreAuthorize("hasAuthority('administration:users:create')")
  public ResponseEntity<UserDTO> create(@NotNull @Valid @RequestBody UserCreateForm userCreateForm)
    throws Exception {
    return new ResponseEntity<>(userService.create(userCreateForm), HttpStatus.CREATED);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('administration:users:read')")
  public ResponseEntity<UserDTO> get(@PathVariable Long id) {
    UserDTO user = userService.getOne(id);
    if (user == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(user, HttpStatus.OK);
    }
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('administration:users:write')")
  public ResponseEntity<UserDTO> update(@PathVariable Long id,
    @Valid @RequestBody UserUpdateForm userUpdateForm) {
    UserDTO user = userService.getOne(id);
    if (user == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    } else {
      return new ResponseEntity<>(userService.update(id, userUpdateForm), HttpStatus.OK);
    }
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('administration:users:delete')")
  public ResponseEntity<Map<Class<? extends AbstractDTO>, Long>> delete(@PathVariable Long id,
    @RequestParam(value = "force", defaultValue = "false", required = false) boolean force) {
    UserDTO user = this.userService.getOne(id);
    if (user == null) {
      return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    Optional<Map<Class<? extends AbstractDTO>, Long>> result = this.userService.delete(user, force);
    if (!result.isPresent() || result.get().isEmpty()) {
      return new ResponseEntity<>(HttpStatus.OK);
    } else {
      return new ResponseEntity<>(result.get(), HttpStatus.CONFLICT);
    }
  }

}
