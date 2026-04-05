package com.theblood.springfood.media.repository;

import com.theblood.springfood.media.domain.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MediaRepository extends JpaRepository<MediaFile, String> {


}
