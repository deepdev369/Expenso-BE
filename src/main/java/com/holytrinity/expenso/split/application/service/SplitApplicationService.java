package com.holytrinity.expenso.split.application.service;

import com.holytrinity.expenso.split.application.dto.SplitDTO;
import com.holytrinity.expenso.split.application.dto.SplitMemberDTO;
import com.holytrinity.expenso.split.application.port.in.SplitUseCase;
import com.holytrinity.expenso.split.application.port.out.SplitPort;
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

    private final com.holytrinity.expenso.security.UserContext userContext;

    public SplitApplicationService(SplitPort splitPort, UserPort userPort, AssociatedUserPort associatedUserPort, com.holytrinity.expenso.security.UserContext userContext) {
        this.splitPort = splitPort;
        this.userPort = userPort;
        this.associatedUserPort = associatedUserPort;
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
        
        split.setDescription(dto.getDescription());
        split.setTotalAmount(dto.getTotalAmount());
        split.setSplitMethod(dto.getSplitMethod());
        split.setSplitDate(dto.getSplitDate());

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
                member.setHasPaid(memberDto.getHasPaid() != null ? memberDto.getHasPaid() : false);
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
        List<SplitMemberDTO> memberDTOs = s.getMembers() == null ? new ArrayList<>() : s.getMembers().stream()
                .map(m -> SplitMemberDTO.builder()
                        .splitMemberId(m.getSplitMemberId())
                        .associatedUserId(m.getAssociatedUser().getAssociatedUserId())
                        .amountOwed(m.getAmountOwed())
                        .hasPaid(m.getHasPaid())
                        .build())
                .collect(Collectors.toList());

        return SplitDTO.builder()
                .splitId(s.getSplitId())
                .version(s.getVersion())
                .deleted(s.getDeleted())
                .description(s.getDescription())
                .totalAmount(s.getTotalAmount())
                .splitMethod(s.getSplitMethod())
                .splitDate(s.getSplitDate())
                .creatorUserId(s.getCreatorUser().getUserId())
                .members(memberDTOs)
                .dateCreated(s.getDateCreated() != null ? s.getDateCreated().toInstant().toEpochMilli() : null)
                .lastUpdated(s.getLastUpdated() != null ? s.getLastUpdated().toInstant().toEpochMilli() : null)
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
            split.setDescription(dto.getDescription());
            split.setTotalAmount(dto.getTotalAmount());
            split.setSplitMethod(dto.getSplitMethod());
            split.setSplitDate(dto.getSplitDate());

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
                    member.setHasPaid(memberDto.getHasPaid() != null ? memberDto.getHasPaid() : false);
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

        return savedSplits.stream().map(s -> {
            List<SplitMemberDTO> memberDTOs = s.getMembers() == null ? new ArrayList<>() : s.getMembers().stream()
                    .map(m -> SplitMemberDTO.builder()
                            .splitMemberId(m.getSplitMemberId())
                            .associatedUserId(m.getAssociatedUser().getAssociatedUserId())
                            .amountOwed(m.getAmountOwed())
                            .hasPaid(m.getHasPaid())
                            .build())
                    .collect(Collectors.toList());

            return SplitDTO.builder()
                    .splitId(s.getSplitId())
                    .version(s.getVersion())
                    .deleted(s.getDeleted())
                    .description(s.getDescription())
                    .totalAmount(s.getTotalAmount())
                    .splitMethod(s.getSplitMethod())
                    .splitDate(s.getSplitDate())
                    .creatorUserId(s.getCreatorUser().getUserId())
                    .members(memberDTOs)
                    .dateCreated(s.getDateCreated() != null ? s.getDateCreated().toInstant().toEpochMilli() : null)
                    .lastUpdated(s.getLastUpdated() != null ? s.getLastUpdated().toInstant().toEpochMilli() : null)
                    .build();
        }).collect(Collectors.toList());
    }
}
