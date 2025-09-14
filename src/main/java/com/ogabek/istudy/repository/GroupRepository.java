package com.ogabek.istudy.repository;

import com.ogabek.istudy.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    // Fetch groups with all related entities eagerly loaded
    @Query("SELECT g FROM Group g " +
            "LEFT JOIN FETCH g.course " +
            "LEFT JOIN FETCH g.teacher " +
            "LEFT JOIN FETCH g.branch " +
            "WHERE g.branch.id = :branchId")
    List<Group> findByBranchIdWithAllRelations(@Param("branchId") Long branchId);

    // Fetch single group with all related entities
    @Query("SELECT g FROM Group g " +
            "LEFT JOIN FETCH g.course " +
            "LEFT JOIN FETCH g.teacher " +
            "LEFT JOIN FETCH g.branch " +
            "LEFT JOIN FETCH g.students " +
            "WHERE g.id = :groupId")
    Optional<Group> findByIdWithAllRelations(@Param("groupId") Long groupId);

    // Fetch group with students only
    @Query("SELECT g FROM Group g " +
            "LEFT JOIN FETCH g.students " +
            "LEFT JOIN FETCH g.course " +
            "LEFT JOIN FETCH g.teacher " +
            "LEFT JOIN FETCH g.branch " +
            "WHERE g.id = :groupId")
    Group findByIdWithStudents(@Param("groupId") Long groupId);

    // Fetch groups by teacher with relations
    @Query("SELECT g FROM Group g " +
            "LEFT JOIN FETCH g.course " +
            "LEFT JOIN FETCH g.teacher " +
            "LEFT JOIN FETCH g.branch " +
            "WHERE g.teacher.id = :teacherId")
    List<Group> findByTeacherIdWithRelations(@Param("teacherId") Long teacherId);

    // Fetch groups by course with relations
    @Query("SELECT g FROM Group g " +
            "LEFT JOIN FETCH g.course " +
            "LEFT JOIN FETCH g.teacher " +
            "LEFT JOIN FETCH g.branch " +
            "WHERE g.course.id = :courseId")
    List<Group> findByCourseIdWithRelations(@Param("courseId") Long courseId);

    // Fetch groups by branch and teacher with relations
    @Query("SELECT g FROM Group g " +
            "LEFT JOIN FETCH g.course " +
            "LEFT JOIN FETCH g.teacher " +
            "LEFT JOIN FETCH g.branch " +
            "WHERE g.branch.id = :branchId AND g.teacher.id = :teacherId")
    List<Group> findByBranchIdAndTeacherIdWithRelations(@Param("branchId") Long branchId, @Param("teacherId") Long teacherId);

    // Find groups with unpaid students for specific month (with relations)
    @Query("SELECT DISTINCT g FROM Group g " +
            "LEFT JOIN FETCH g.course " +
            "LEFT JOIN FETCH g.teacher " +
            "LEFT JOIN FETCH g.branch " +
            "JOIN g.students s " +
            "WHERE g.branch.id = :branchId AND " +
            "s.id NOT IN (SELECT DISTINCT p.student.id FROM Payment p WHERE p.paymentYear = :year AND p.paymentMonth = :month)")
    List<Group> findGroupsWithUnpaidStudentsWithRelations(@Param("branchId") Long branchId,
                                                          @Param("year") int year, @Param("month") int month);

    // Keep original methods for backward compatibility (may cause lazy loading issues)
    List<Group> findByBranchId(Long branchId);
    List<Group> findByTeacherId(Long teacherId);
    List<Group> findByCourseId(Long courseId);
    List<Group> findByBranchIdAndTeacherId(Long branchId, Long teacherId);

    @Query("SELECT DISTINCT g FROM Group g JOIN g.students s WHERE g.branch.id = :branchId AND " +
            "s.id NOT IN (SELECT DISTINCT p.student.id FROM Payment p WHERE p.paymentYear = :year AND p.paymentMonth = :month)")
    List<Group> findGroupsWithUnpaidStudents(@Param("branchId") Long branchId,
                                             @Param("year") int year, @Param("month") int month);
}