package com.example.demo.repository;

import com.example.demo.entity.FileDB;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileDBRepository extends JpaRepository<FileDB, String> {
 public FileDB findByName(String fileName);
}