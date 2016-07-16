package com.lyubenblagoev.postfixrest.repository;

import java.io.Serializable;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.NoRepositoryBean;
  
import com.lyubenblagoev.postfixrest.entity.Bcc;

@NoRepositoryBean
public interface BccRepository<T extends Bcc, ID extends Serializable> extends CrudRepository<T, ID> {
	T findByAccountId(ID accountId);
}
