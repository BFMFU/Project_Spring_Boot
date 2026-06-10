package app.product.project.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "job_posting")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobPosting {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String title;
	private String description;
	private String salaryRange;

	@ManyToOne
	@JoinColumn(name = "employer_id")
	private Users employer;

	@ManyToOne
	@JoinColumn(name = "job_status_id")
	private JobStatus status;

	@OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true)
	@JsonIgnore
	private List<Application> applications;

}
