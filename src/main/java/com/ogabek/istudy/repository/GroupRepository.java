package com.ogabek.istudy.repository;

import com.ogabek.istudy.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByBranchId(Long branchId);
    List<Group> findByTeacherId(Long teacherId);
    List<Group> findByCourseId(Long courseId);
    List<Group> findByBranchIdAndTeacherId(Long branchId, Long teacherId);
    
    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.students WHERE g.id = :groupId")
    Group findByIdWithStudents(@Param("groupId") Long groupId);
    
    // Find groups with unpaid students for specific month
    @Query("SELECT DISTINCT g FROM Group g JOIN g.students s WHERE g.branch.id = :branchId AND " +
           "s.id NOT IN (SELECT DISTINCT p.student.id FROM Payment p WHERE p.paymentYear = :year AND p.paymentMonth = :month)")
    List<Group> findGroupsWithUnpaidStudents(@Param("branchId") Long branchId, 
                                           @Param("year") int year, @Param("month") int month);
}