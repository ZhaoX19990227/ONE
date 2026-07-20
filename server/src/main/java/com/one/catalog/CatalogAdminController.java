package com.one.catalog;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/catalog/custom-entries")
public class CatalogAdminController {
    private static final String ADMIN_HEADER = "X-ONE-Admin-Secret";
    private final CatalogAdminService service;
    public CatalogAdminController(CatalogAdminService service) { this.service = service; }

    @GetMapping
    public CatalogAdminDtos.PendingList pending(@RequestHeader(value = ADMIN_HEADER, required = false) String secret) {
        return service.pending(secret);
    }
    @PostMapping("/{id}/normalize")
    public CatalogAdminDtos.NormalizeResult normalize(@RequestHeader(value = ADMIN_HEADER, required = false) String secret,
                                                       @PathVariable long id,
                                                       @Valid @RequestBody CatalogAdminDtos.NormalizeRequest request) {
        return service.normalize(secret, id, request.targetItemId());
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ignore(@RequestHeader(value = ADMIN_HEADER, required = false) String secret, @PathVariable long id) {
        service.ignore(secret, id);
    }
}
