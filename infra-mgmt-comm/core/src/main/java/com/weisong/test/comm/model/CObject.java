package com.weisong.test.comm.model;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
abstract public class CObject implements Serializable {

	private static final long serialVersionUID = 1L;

	protected CObject() {}

	public CObject(CObject o) {
	}

}
