//package com.viettel.dvs.actionlog.service.util;
//
//import com.viettel.dvs.client.api.PartyOrganizationClient;
//import lombok.AllArgsConstructor;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import java.util.*;
//
//@Service
//@AllArgsConstructor
//public class OrganizationService {
//    private static final Logger LOG = LoggerFactory.getLogger(OrganizationService.class);
//    private final PartyOrganizationClient partyOrganizationClient;
//
//    public PartyOrganizationClient.OrganizationDto getOne(String id) {
//        try {
//            var d = partyOrganizationClient.getOrganization(id);
//            return d.getBody();
//        } catch (Exception e) {
//            LOG.error("Error getting party organization details getOne: {}", e.getMessage());
//        }
//        return null;
//    }
//
//    public List<PartyOrganizationClient.OrganizationDto> getMultiple(Set<String> ids) {
//        if (!ids.isEmpty()) {
//            try {
//                var orgList = partyOrganizationClient.listOrganizationsByIds(String.join(",", ids));
//                return orgList.getBody();
//            } catch (Exception e) {
//                LOG.error("Error getting party organization details getMultiple: {}", e.getMessage());
//            }
//        }
//
//        return null;
//    }
//
//    public Map<String, PartyOrganizationClient.OrganizationDto> getMapChild(Set<String> ids) {
//        Map<String, PartyOrganizationClient.OrganizationDto> mapOrg = new HashMap<>();
//        var organizations = getMultiple(ids);
//        if (organizations != null) {
//            for (PartyOrganizationClient.OrganizationDto dto : organizations) {
//                mapOrg.put(dto.getId(), dto);
//            }
//        }
//        return mapOrg;
//    }
//
//    public Map<String, PartyOrganizationClient.OrganizationDto> getMapChildAndParent(Set<String> ids) {
//        Map<String, PartyOrganizationClient.OrganizationDto> mapOrg = new HashMap<>();
//
//        var childOrg = getMultiple(ids);
//        if (childOrg != null) {
//            Set<String> parentOrgSet = new HashSet<>();
//            for (PartyOrganizationClient.OrganizationDto dto : childOrg) {
//                mapOrg.put(dto.getId(), dto);
//                if (StringUtils.hasText(dto.getOrganizationParentId())) {
//                    parentOrgSet.add(dto.getOrganizationParentId());
//                }
//            }
//            if (!parentOrgSet.isEmpty()) {
//                var parent = getMultiple(parentOrgSet);
//                if (parent != null) {
//                    for (PartyOrganizationClient.OrganizationDto dto : parent) {
//                        mapOrg.put(dto.getId(), dto);
//                    }
//                }
//
//            }
//        }
//        return mapOrg;
//    }
//
//    public String getOrgName(String shopId) {
//        var org = getOne(shopId);
//        if (org != null) {
//            if (StringUtils.hasText(org.getOrganizationFullName())) {
//                return org.getOrganizationFullName();
//            }
//            return org.getOrganizationName();
//        }
//        return null;
//    }
//}
