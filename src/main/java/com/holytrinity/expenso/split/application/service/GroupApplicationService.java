package com.holytrinity.expenso.split.application.service;

import com.holytrinity.expenso.split.application.dto.GroupDTO;
import com.holytrinity.expenso.split.application.port.in.GroupUseCase;
import com.holytrinity.expenso.split.application.port.out.GroupPort;
import com.holytrinity.expenso.split.domain.Group;
import com.holytrinity.expenso.user.application.port.out.AssociatedUserPort;
import com.holytrinity.expenso.user.application.port.out.UserPort;
import com.holytrinity.expenso.user.domain.AssociatedUser;
import com.holytrinity.expenso.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupApplicationService implements GroupUseCase {

    private final GroupPort groupPort;
    private final UserPort userPort;
    private final AssociatedUserPort associatedUserPort;
    private final com.holytrinity.expenso.security.UserContext userContext;

    public GroupApplicationService(GroupPort groupPort, UserPort userPort, AssociatedUserPort associatedUserPort, com.holytrinity.expenso.security.UserContext userContext) {
        this.groupPort = groupPort;
        this.userPort = userPort;
        this.associatedUserPort = associatedUserPort;
        this.userContext = userContext;
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<GroupDTO> findAll(org.springframework.data.domain.Pageable pageable) {
        return groupPort.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupDTO get(String id) {
        Group group = groupPort.loadGroup(id)
                .orElseThrow(com.holytrinity.expenso.shared.exception.NotFoundException::new);
        checkOwnership(group);
        return mapToDTO(group);
    }

    @Override
    @Transactional
    public GroupDTO update(String id, GroupDTO dto) {
        Group group = groupPort.loadGroup(id).orElse(new Group());
        if (group.getId() == null) {
            group.setId(id);
            User user = userPort.loadUser(userContext.getCurrentUserId())
                    .orElseThrow(com.holytrinity.expenso.shared.exception.NotFoundException::new);
            group.setUser(user);
        } else {
            checkOwnership(group);
        }
        
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setDeleted(dto.getDeleted() != null ? dto.getDeleted() : false);
        
        List<AssociatedUser> members = new ArrayList<>();
        if (dto.getMemberIds() != null) {
            for (String memberId : dto.getMemberIds()) {
                AssociatedUser member = associatedUserPort.loadAssociatedUser(memberId)
                        .orElseThrow(() -> new IllegalArgumentException("AssociatedUser not found: " + memberId));
                members.add(member);
            }
        }
        group.setMembers(members);

        Group updatedGroup = groupPort.saveAllGroups(List.of(group)).get(0);
        return mapToDTO(updatedGroup);
    }

    @Override
    @Transactional
    public void delete(String id) {
        Group group = groupPort.loadGroup(id)
                .orElseThrow(com.holytrinity.expenso.shared.exception.NotFoundException::new);
        checkOwnership(group);
        group.setDeleted(true);
        groupPort.saveAllGroups(List.of(group));
    }

    private GroupDTO mapToDTO(Group g) {
        return GroupDTO.builder()
                .id(g.getId())
                .name(g.getName())
                .description(g.getDescription())
                .memberIds(g.getMembers() == null ? new ArrayList<>() : g.getMembers().stream().map(AssociatedUser::getAssociatedUserId).collect(Collectors.toList()))
                .deleted(g.getDeleted())
                .createdAt(g.getCreatedAt())
                .updatedAt(g.getUpdatedAt())
                .build();
    }

    private void checkOwnership(Group group) {
        String currentUserId = userContext.getCurrentUserId();
        if (!group.getUser().getUserId().equals(currentUserId)) {
            throw new com.holytrinity.expenso.shared.exception.NotFoundException();
        }
    }
}
