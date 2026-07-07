package com.holytrinity.expenso.split.application.service;

import com.holytrinity.expenso.split.application.dto.RepaymentDTO;
import com.holytrinity.expenso.split.application.dto.SplitDTO;
import com.holytrinity.expenso.split.application.dto.SplitMemberDTO;
import com.holytrinity.expenso.split.application.port.in.SplitUseCase;
import com.holytrinity.expenso.split.application.port.out.SplitPort;
import com.holytrinity.expenso.split.application.port.out.GroupPort;
import com.holytrinity.expenso.split.application.port.out.SplitAiExtractionPort;
import com.holytrinity.expenso.split.application.port.out.dto.SplitAiExtractionRequest;
import com.holytrinity.expenso.split.application.dto.SplitExtractionRequest;
import com.holytrinity.expenso.split.domain.Group;
import com.holytrinity.expenso.split.domain.SettlementStatus;
import com.holytrinity.expenso.split.domain.Split;
import com.holytrinity.expenso.split.domain.SplitMember;
import com.holytrinity.expenso.user.application.port.out.AssociatedUserPort;
import com.holytrinity.expenso.user.application.port.out.UserPort;
import com.holytrinity.expenso.user.domain.AssociatedUser;
import com.holytrinity.expenso.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SplitApplicationService implements SplitUseCase {

    private final SplitPort splitPort;
    private final UserPort userPort;
    private final AssociatedUserPort associatedUserPort;
    private final GroupPort groupPort;
    private final SplitAiExtractionPort splitAiExtractionPort;

    private final com.holytrinity.expenso.security.UserContext userContext;

    public SplitApplicationService(SplitPort splitPort, UserPort userPort, AssociatedUserPort associatedUserPort, GroupPort groupPort, SplitAiExtractionPort splitAiExtractionPort, com.holytrinity.expenso.security.UserContext userContext) {
        this.splitPort = splitPort;
        this.userPort = userPort;
        this.associatedUserPort = associatedUserPort;
        this.groupPort = groupPort;
        this.splitAiExtractionPort = splitAiExtractionPort;
        this.userContext = userContext;
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<SplitDTO> findAll(org.springframework.data.domain.Pageable pageable) {
        return splitPort.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public SplitDTO get(String splitId) {
        Split split = splitPort.loadSplit(splitId)
                .orElseThrow(com.holytrinity.expenso.shared.exception.NotFoundException::new);
        checkOwnership(split);
        return mapToDTO(split);
    }

    @Override
    @Transactional
    public SplitDTO update(String splitId, SplitDTO dto) {
        Split split = splitPort.loadSplit(splitId)
                .orElseThrow(com.holytrinity.expenso.shared.exception.NotFoundException::new);
        checkOwnership(split);
        
        split.setTitle(dto.getTitle());
        split.setDescription(dto.getDescription());
        split.setTotalAmount(dto.getTotalAmount());
        split.setSplitMethod(dto.getSplitMethod());
        split.setSplitDate(dto.getSplitDate());
        split.setSettlementStatus(dto.getSettlementStatus() != null ? dto.getSettlementStatus() : SettlementStatus.UNSETTLED);

        if (dto.getPaidById() != null) {
            AssociatedUser paidBy = associatedUserPort.loadAssociatedUser(dto.getPaidById())
                    .orElseThrow(() -> new IllegalArgumentException("AssociatedUser not found: " + dto.getPaidById()));
            split.setPaidBy(paidBy);
        }

        if (dto.getGroupId() != null) {
            Group group = groupPort.loadGroup(dto.getGroupId())
                    .orElseThrow(() -> new IllegalArgumentException("Group not found: " + dto.getGroupId()));
            split.setGroup(group);
        } else {
            split.setGroup(null);
        }

        List<SplitMember> currentMembers = split.getMembers() == null ? new ArrayList<>() : split.getMembers();
        Map<String, SplitMember> currentMemberMap = currentMembers.stream()
                .collect(Collectors.toMap(SplitMember::getSplitMemberId, m -> m));

        List<SplitMember> updatedMembers = new ArrayList<>();
        if (dto.getMembers() != null) {
            for (SplitMemberDTO memberDto : dto.getMembers()) {
                SplitMember member = currentMemberMap.get(memberDto.getSplitMemberId());
                if (member == null) {
                    member = new SplitMember();
                    member.setSplitMemberId(memberDto.getSplitMemberId());
                    member.setSplit(split);
                    
                    AssociatedUser associatedUser = associatedUserPort.loadAssociatedUser(memberDto.getAssociatedUserId())
                            .orElseThrow(() -> new IllegalArgumentException("AssociatedUser not found: " + memberDto.getAssociatedUserId()));
                    member.setAssociatedUser(associatedUser);
                }
                member.setAmountOwed(memberDto.getAmountOwed());
                member.setRationale(memberDto.getRationale());
                member.setSettlementStatus(memberDto.getSettlementStatus() != null ? memberDto.getSettlementStatus() : SettlementStatus.UNSETTLED);
                member.setAmountSettled(memberDto.getAmountSettled() != null ? memberDto.getAmountSettled() : 0.0);
                member.setAmountRemaining(memberDto.getAmountRemaining() != null ? memberDto.getAmountRemaining() : member.getAmountOwed());
                updatedMembers.add(member);
            }
        }
        
        if (split.getMembers() != null) {
            split.getMembers().clear();
            split.getMembers().addAll(updatedMembers);
        } else {
            split.setMembers(updatedMembers);
        }

        Split updatedSplit = splitPort.saveAllSplits(List.of(split)).get(0);
        return mapToDTO(updatedSplit);
    }

    @Override
    @Transactional
    public void delete(String splitId) {
        Split split = splitPort.loadSplit(splitId)
                .orElseThrow(com.holytrinity.expenso.shared.exception.NotFoundException::new);
        checkOwnership(split);
        split.setDeleted(true);
        splitPort.saveAllSplits(List.of(split));
    }

    @Override
    @Transactional
    public void deleteBulk(List<String> splitIds) {
        splitIds.forEach(id -> {
            splitPort.loadSplit(id).ifPresent(split -> {
                delete(split.getSplitId());
            });
        });
    }

    private SplitDTO mapToDTO(Split s) {
        List<SplitMemberDTO> memberDTOs = s.getMembers() == null ? new ArrayList<SplitMemberDTO>() : s.getMembers().stream()
                .map(m -> SplitMemberDTO.builder()
                        .splitMemberId(m.getSplitMemberId())
                        .associatedUserId(m.getAssociatedUser().getAssociatedUserId())
                        .amountOwed(m.getAmountOwed())
                        .rationale(m.getRationale())
                        .settlementStatus(m.getSettlementStatus())
                        .amountSettled(m.getAmountSettled())
                        .amountRemaining(m.getAmountRemaining())
                        .repayments(m.getRepayments() == null ? new ArrayList<RepaymentDTO>() : m.getRepayments().stream()
                                .map(r -> RepaymentDTO.builder()
                                        .id(r.getId())
                                        .splitMemberId(r.getSplitParticipant().getSplitMemberId())
                                        .splitId(r.getSplit().getSplitId())
                                        .amount(r.getAmount())
                                        .method(r.getMethod())
                                        .note(r.getNote())
                                        .repaidAt(r.getRepaidAt())
                                        .createdAt(r.getCreatedAt())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        return SplitDTO.builder()
                .splitId(s.getSplitId())
                .version(s.getVersion())
                .deleted(s.getDeleted())
                .title(s.getTitle())
                .description(s.getDescription())
                .totalAmount(s.getTotalAmount())
                .splitMethod(s.getSplitMethod())
                .splitDate(s.getSplitDate())
                .creatorUserId(s.getCreatorUser().getUserId())
                .paidById(s.getPaidBy() != null ? s.getPaidBy().getAssociatedUserId() : null)
                .groupId(s.getGroup() != null ? s.getGroup().getId() : null)
                .settlementStatus(s.getSettlementStatus())
                .members(memberDTOs)
                .dateCreated(s.getDateCreated() != null ? s.getDateCreated() : null)
                .lastUpdated(s.getLastUpdated() != null ? s.getLastUpdated() : null)
                .build();
    }

    private void checkOwnership(Split split) {
        String currentUserId = userContext.getCurrentUserId();
        if (!split.getCreatorUser().getUserId().equals(currentUserId)) {
            throw new com.holytrinity.expenso.shared.exception.NotFoundException();
        }
    }

    @Override
    @Transactional
    public List<SplitDTO> processBulkSplits(String userId, List<SplitDTO> incomingSplits) {
        User user = userPort.loadUser(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<String> incomingIds = incomingSplits.stream().map(SplitDTO::getSplitId).collect(Collectors.toList());
        List<Split> existingSplits = incomingIds.isEmpty() ? new ArrayList<>() : splitPort.findAllWithDeletedByIdsAndUserId(incomingIds, userId);
        Map<String, Split> existingSplitMap = existingSplits.stream()
                .collect(Collectors.toMap(Split::getSplitId, s -> s));

        List<Split> splitsToSave = new ArrayList<>();

        for (SplitDTO dto : incomingSplits) {
            Split split = existingSplitMap.get(dto.getSplitId());
            if (split == null) {
                split = new Split();
                split.setSplitId(dto.getSplitId());
                split.setCreatorUser(user);
            }

            split.setDeleted(dto.getDeleted() != null ? dto.getDeleted() : false);
            split.setTitle(dto.getTitle());
            split.setDescription(dto.getDescription());
            split.setTotalAmount(dto.getTotalAmount());
            split.setSplitMethod(dto.getSplitMethod());
            split.setSplitDate(dto.getSplitDate());
            split.setSettlementStatus(dto.getSettlementStatus() != null ? dto.getSettlementStatus() : SettlementStatus.UNSETTLED);

            if (dto.getPaidById() != null) {
                AssociatedUser paidBy = associatedUserPort.loadAssociatedUser(dto.getPaidById())
                        .orElseThrow(() -> new IllegalArgumentException("AssociatedUser not found: " + dto.getPaidById()));
                split.setPaidBy(paidBy);
            }

            if (dto.getGroupId() != null) {
                Group group = groupPort.loadGroup(dto.getGroupId())
                        .orElseThrow(() -> new IllegalArgumentException("Group not found: " + dto.getGroupId()));
                split.setGroup(group);
            } else {
                split.setGroup(null);
            }

            // Process members
            List<SplitMember> currentMembers = split.getMembers() == null ? new ArrayList<>() : split.getMembers();
            Map<String, SplitMember> currentMemberMap = currentMembers.stream()
                    .collect(Collectors.toMap(SplitMember::getSplitMemberId, m -> m));

            List<SplitMember> updatedMembers = new ArrayList<>();
            if (dto.getMembers() != null) {
                for (SplitMemberDTO memberDto : dto.getMembers()) {
                    SplitMember member = currentMemberMap.get(memberDto.getSplitMemberId());
                    if (member == null) {
                        member = new SplitMember();
                        member.setSplitMemberId(memberDto.getSplitMemberId());
                        member.setSplit(split);
                        
                        AssociatedUser associatedUser = associatedUserPort.loadAssociatedUser(memberDto.getAssociatedUserId())
                                .orElseThrow(() -> new IllegalArgumentException("AssociatedUser not found: " + memberDto.getAssociatedUserId()));
                        member.setAssociatedUser(associatedUser);
                    }
                    member.setAmountOwed(memberDto.getAmountOwed());
                    member.setRationale(memberDto.getRationale());
                    member.setSettlementStatus(memberDto.getSettlementStatus() != null ? memberDto.getSettlementStatus() : SettlementStatus.UNSETTLED);
                    member.setAmountSettled(memberDto.getAmountSettled() != null ? memberDto.getAmountSettled() : 0.0);
                    member.setAmountRemaining(memberDto.getAmountRemaining() != null ? memberDto.getAmountRemaining() : member.getAmountOwed());
                    
                    if (memberDto.getRepayments() != null) {
                        List<com.holytrinity.expenso.split.domain.Repayment> currentRepayments = member.getRepayments();
                        Map<String, com.holytrinity.expenso.split.domain.Repayment> currentRepaymentMap = currentRepayments.stream()
                                .collect(Collectors.toMap(com.holytrinity.expenso.split.domain.Repayment::getId, r -> r));
                        
                        List<com.holytrinity.expenso.split.domain.Repayment> updatedRepayments = new ArrayList<>();
                        for (RepaymentDTO repDto : memberDto.getRepayments()) {
                            com.holytrinity.expenso.split.domain.Repayment rep = currentRepaymentMap.get(repDto.getId());
                            if (rep == null) {
                                rep = new com.holytrinity.expenso.split.domain.Repayment();
                                rep.setId(repDto.getId());
                                rep.setSplitParticipant(member);
                                rep.setSplit(split);
                            }
                            rep.setAmount(repDto.getAmount());
                            rep.setMethod(repDto.getMethod());
                            rep.setNote(repDto.getNote());
                            rep.setRepaidAt(repDto.getRepaidAt());
                            rep.setCreatedAt(repDto.getCreatedAt());
                            updatedRepayments.add(rep);
                        }
                        member.getRepayments().clear();
                        member.getRepayments().addAll(updatedRepayments);
                    }

                    updatedMembers.add(member);
                }
            }
            
            // clear and add to maintain orphan removal
            if (split.getMembers() != null) {
                split.getMembers().clear();
                split.getMembers().addAll(updatedMembers);
            } else {
                split.setMembers(updatedMembers);
            }

            splitsToSave.add(split);
        }

        List<Split> savedSplits = splitPort.saveAllSplits(splitsToSave);
        return savedSplits.stream().map(this::mapToDTO).collect(Collectors.toList());
    }
    @Override
    public SplitDTO submitForExtraction(SplitExtractionRequest request) {
        String currentUserId = userContext.getCurrentUserId();
        User currentUser = userPort.loadUser(currentUserId)
                .orElseThrow(() -> new com.holytrinity.expenso.shared.exception.NotFoundException("User not found"));

        SplitAiExtractionRequest aiRequest = SplitAiExtractionRequest.builder()
                .userId(currentUserId)
                .rawText(request.getRawText())
                .file(request.getFile())
                .currency(request.getCurrency() != null ? request.getCurrency() : currentUser.getDefaultCurrency())
                .build();

        if (request.getFriendsJson() != null && !request.getFriendsJson().isBlank()) {
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<java.util.Map<String, String>> friends = mapper.readValue(
                        request.getFriendsJson(),
                        new com.fasterxml.jackson.core.type.TypeReference<>() {}
                );
                aiRequest.setFriends(friends);
            } catch (Exception e) {
                // ignore
            }
        }

        com.fasterxml.jackson.databind.JsonNode payload = splitAiExtractionPort.submitSplitForExtraction(aiRequest);
        return processAiExtractionResult(payload, currentUserId);
    }

    private SplitDTO processAiExtractionResult(com.fasterxml.jackson.databind.JsonNode payload, String currentUserId) {
        String status = payload.path("status").asText("");
        boolean success = "COMPLETED".equals(status);

        if (!success) {
            throw new RuntimeException("AI Extraction failed: " + payload.path("error").asText());
        }

        com.fasterxml.jackson.databind.JsonNode result = payload.path("result");
        com.fasterxml.jackson.databind.JsonNode extractedData = result.path("extracted_data");

        if (extractedData.isMissingNode() || extractedData.isNull()) {
            throw new RuntimeException("AI extraction returned COMPLETED but no data was found");
        }

        SplitDTO dto = new SplitDTO();
        dto.setSplitId(java.util.UUID.randomUUID().toString());
        dto.setTitle(extractedData.path("title").asText("Untitled Split"));
        dto.setDescription(extractedData.path("description").asText(""));
        dto.setTotalAmount(extractedData.path("totalAmount").asDouble(0.0));
        dto.setSplitMethod(com.holytrinity.expenso.split.domain.SplitMethod.valueOf(extractedData.path("splitMethod").asText("EQUAL")));
        dto.setSettlementStatus(com.holytrinity.expenso.split.domain.SettlementStatus.UNSETTLED);
        dto.setCreatorUserId(currentUserId);
        dto.setSplitDate(java.time.OffsetDateTime.now());
        // For simplicity, we won't fully persist the extracted split to DB here
        // as the frontend handles confirmation. We will just return the DTO.

        List<SplitMemberDTO> members = new ArrayList<>();
        com.fasterxml.jackson.databind.JsonNode membersNode = extractedData.path("members");
        if (membersNode.isArray()) {
            for (com.fasterxml.jackson.databind.JsonNode memberNode : membersNode) {
                SplitMemberDTO m = new SplitMemberDTO();
                m.setSplitMemberId(java.util.UUID.randomUUID().toString());
                
                String name = memberNode.path("name").asText("Unknown");
                m.setAssociatedUserId(name); // FE handles resolving name back to ID if needed
                
                m.setAmountOwed(memberNode.path("amountOwed").asDouble(0.0));
                m.setRationale(memberNode.path("rationale").asText(""));
                
                boolean hasPaid = memberNode.path("has_paid").asBoolean(false);
                m.setSettlementStatus(hasPaid ? com.holytrinity.expenso.split.domain.SettlementStatus.SETTLED : com.holytrinity.expenso.split.domain.SettlementStatus.UNSETTLED);
                m.setAmountSettled(hasPaid ? m.getAmountOwed() : 0.0);
                m.setAmountRemaining(hasPaid ? 0.0 : m.getAmountOwed());
                
                members.add(m);
            }
        }
        
        dto.setMembers(members);
        return dto;
    }
}
