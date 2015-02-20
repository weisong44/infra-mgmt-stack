package com.weisong.test.comm.model;

import java.sql.Timestamp;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Version;

import lombok.Getter;
import lombok.ToString;

@MappedSuperclass
@ToString
abstract public class CDatabaseObject extends CObject {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Getter
	private Long id;
	
	@Getter
	@Version
	private Timestamp updatedAt;

	protected CDatabaseObject() {}

	public CDatabaseObject(CDatabaseObject o) {
		super(o);
		this.id = o.id;
		this.updatedAt = o.updatedAt;
	}

}
