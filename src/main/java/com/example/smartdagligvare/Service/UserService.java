package com.example.smartdagligvare.Service;

import com.example.smartdagligvare.Dto.UserRequest;
import com.example.smartdagligvare.Model.AppUser;

import java.util.List;

public interface UserService {

    List<AppUser> findAll();

    AppUser findById(Long id);

    AppUser save(UserRequest request);

    void deleteById(Long id);

    AppUser update(Long id, UserRequest request);


}
