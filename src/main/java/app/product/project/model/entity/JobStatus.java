package app.product.project.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "job_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatus {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "status_name", length = 100, nullable = false)
	private String statusName;

}
