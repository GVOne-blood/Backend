package com.theblood.notification.web.rest;

import com.theblood.notification.repository.NotificationRepository;
import com.theblood.notification.service.NotificationService;
import com.theblood.notification.service.dto.DeleteNotificationResponseDTO;
import com.theblood.notification.service.dto.NotificationDTO;
import com.theblood.notification.service.dto.ViewNotificationDTO;
import com.theblood.notification.web.rest.errors.BadRequestAlertException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * REST controller for managing {@link com.theblood.notification.domain.Notification}.
 */
@RestController
@CrossOrigin("*")
@RequestMapping("/api/notifications")
public class NotificationResource {

    private static final Logger LOG = LoggerFactory.getLogger(NotificationResource.class);

    private static final String ENTITY_NAME = "notificationNotifications";
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    public NotificationResource(
        NotificationService notificationService,
        NotificationRepository notificationRepository
    ) {
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
    }

    /**
     * {@code POST  /notifications} : Create a new notifications.
     *
     * @param notificationsDTO the notificationsDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new notificationsDTO, or with status {@code 400 (Bad Request)} if the notifications has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<NotificationDTO> createNotifications(@Valid @RequestBody NotificationDTO notificationsDTO, HttpServletRequest request)
        throws URISyntaxException {
        LOG.debug("REST request to save Notifications : {}", notificationsDTO);
        if (notificationsDTO.getNotificationId() != null) {
            throw new BadRequestAlertException("A new partyMember cannot already have an ID", ENTITY_NAME, "idexists");
        }
        notificationsDTO = notificationService.save(notificationsDTO, request);
        return ResponseEntity.created(new URI("/api/notifications/" + notificationsDTO.getNotificationId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, notificationsDTO.getNotificationId()))
            .body(notificationsDTO);
    }

    /**
     * {@code PUT  /notifications/:notificationId} : Updates an existing notifications.
     *
     * @param notificationId   the id of the notificationsDTO to save.
     * @param notificationsDTO the notificationsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated notificationsDTO,
     * or with status {@code 400 (Bad Request)} if the notificationsDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the notificationsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{notificationId}")
    public ResponseEntity<NotificationDTO> updateNotifications(
        @PathVariable(value = "notificationId", required = false) final String notificationId,
        @Valid @RequestBody NotificationDTO notificationsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Notifications : {}, {}", notificationId, notificationsDTO);
        if (notificationsDTO.getNotificationId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(notificationId, notificationsDTO.getNotificationId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!notificationRepository.existsById(notificationId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        notificationsDTO = notificationService.update(notificationsDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, notificationsDTO.getNotificationId()))
            .body(notificationsDTO);
    }

    /**
     * {@code PATCH  /notifications/:notificationId} : Partial updates given fields of an existing notifications, field will ignore if it is null
     *
     * @param notificationId   the id of the notificationsDTO to save.
     * @param notificationsDTO the notificationsDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated notificationsDTO,
     * or with status {@code 400 (Bad Request)} if the notificationsDTO is not valid,
     * or with status {@code 404 (Not Found)} if the notificationsDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the notificationsDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{notificationId}", consumes = {"application/json", "application/merge-patch+json"})
    public ResponseEntity<NotificationDTO> partialUpdateNotifications(
        @PathVariable(value = "notificationId", required = false) final String notificationId,
        @NotNull @RequestBody NotificationDTO notificationsDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Notifications partially : {}, {}", notificationId, notificationsDTO);
        if (notificationsDTO.getNotificationId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(notificationId, notificationsDTO.getNotificationId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!notificationRepository.existsById(notificationId)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<NotificationDTO> result = notificationService.partialUpdate(notificationsDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, notificationsDTO.getNotificationId())
        );
    }


    /**
     * {@code GET  /notifications/:id} : get the "id" notifications.
     *
     * @param id the id of the notificationsDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the notificationsDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<NotificationDTO> getNotifications(@PathVariable("id") String id) {
        LOG.debug("REST request to get Notifications : {}", id);
        Optional<NotificationDTO> notificationsDTO = notificationService.findOne(id);
        return ResponseUtil.wrapOrNotFound(notificationsDTO);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countUserUnreadNotifications() {
        LOG.debug("REST request to get user's unread Notifications");
        return ResponseEntity.ok().body(notificationService.countUnreadNotification());
    }

    @GetMapping("")
    public ResponseEntity<Page<NotificationDTO>> getUserNotifications(
        @PageableDefault Pageable pageable
    ) {
        LOG.debug("REST request to get user Notifications");
        return ResponseEntity.ok().body(notificationService.getUserNotification(pageable));
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<ViewNotificationDTO> viewDetails(@PathVariable("id") String id) {
        LOG.debug("REST request to view details (direct) Notifications : {}", id);
        ViewNotificationDTO viewNotificationDTO = notificationService.viewNotification(id);
        return ResponseEntity.ok().body(viewNotificationDTO);
    }

    @PutMapping("/mark-as-read")
    public ResponseEntity<List<NotificationDTO>> markNotificationsAsRead(@RequestBody List<String> ids) {
        LOG.debug("REST request to mark Notifications as read");
        List<NotificationDTO> viewedNotifications = notificationService.markAsRead(ids);
        return ResponseEntity.ok().body(viewedNotifications);

    }

    @PutMapping("/mark-as-clicked")
    public ResponseEntity<List<NotificationDTO>> markNotificationsAsClicked(@RequestBody List<String> ids) {
        LOG.debug("REST request to mark Notifications as clicked");
        List<NotificationDTO> viewedNotifications = notificationService.markAsClicked(ids);
        return ResponseEntity.ok().body(viewedNotifications);

    }

    /**
     * {@code DELETE  /notifications/:id} : delete the "id" notifications.
     *
     * @param id the id of the notificationsDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotifications(@PathVariable("id") String id) {
        LOG.debug("REST request to delete Notifications : {}", id);
        notificationService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }

    @DeleteMapping("/batch")
    public ResponseEntity<DeleteNotificationResponseDTO> deleteBatchNotifications(@RequestBody List<String> ids, HttpServletRequest request) {
        LOG.debug("REST request to delete batch Notifications : {}", ids);
        return ResponseEntity.ok().body(notificationService.deleteBatch(ids, request));
    }

    @PostMapping({"/list", "/bulk"})
    public ResponseEntity<List<NotificationDTO>> createListNotifications(@RequestBody List<NotificationDTO> notificationsDTOS) {
        LOG.debug("REST request to create list Notifications");
        List<NotificationDTO> notificationsDTOs = notificationService.createListNotification(notificationsDTOS);
        return ResponseEntity.ok().body(notificationsDTOs);
    }
}
