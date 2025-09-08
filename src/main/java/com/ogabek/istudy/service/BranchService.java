package com.ogabek.istudy.service;

import com.ogabek.istudy.dto.request.CreateBranchRequest;
import com.ogabek.istudy.dto.response.BranchDto;
import com.ogabek.istudy.entity.Branch;
import com.ogabek.istudy.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BranchService {
    private final BranchRepository branchRepository;

    public List<BranchDto> getAllBranches() {
        return branchRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public BranchDto getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));
        return convertToDto(branch);
    }

    public BranchDto createBranch(CreateBranchRequest request) {
        Branch branch = new Branch();
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        
        Branch savedBranch = branchRepository.save(branch);
        return convertToDto(savedBranch);
    }

    public BranchDto updateBranch(Long id, CreateBranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found with id: " + id));
        
        branch.setName(request.getName());
        branch.setAddress(request.getAddress());
        
        Branch savedBranch = branchRepository.save(branch);
        return convertToDto(savedBranch);
    }

    public void deleteBranch(Long id) {
        if (!branchRepository.existsById(id)) {
            throw new RuntimeException("Branch not found with id: " + id);
        }
        branchRepository.deleteById(id);
    }

    private BranchDto convertToDto(Branch branch) {
        BranchDto dto = new BranchDto();
        dto.setId(branch.getId());
        dto.setName(branch.getName());
        dto.setAddress(branch.getAddress());
        dto.setCreatedAt(branch.getCreatedAt());
        return dto;
    }
}