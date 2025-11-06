package com.example.customeronboarding.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "customer_details")
@Data
public class CustomerDetailsEntity implements Serializable {


	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "cust_id")
	private String custId;

	@NotNull
	@Column(name = "status", length = 50)
	private String status;

	@NotNull
	@Column(name = "mobile_number", length = 12)
	private String mobNo;

	@Column(name = "created_on")
	private Timestamp createdOn;

	@Column(name = "created_by")
	private String createdBy;

	@Column(name = "updated_on")
	private Timestamp updatedOn;

	@Column(name = "updated_by")
	private String updatedBy;
}
