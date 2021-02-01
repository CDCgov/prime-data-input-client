package gov.cdc.usds.simplereport.db.model;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import gov.cdc.usds.simplereport.db.model.auxiliary.AskOnEntrySurvey;
import gov.cdc.usds.simplereport.db.model.auxiliary.TestCorrectionStatus;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Type;

import gov.cdc.usds.simplereport.db.model.auxiliary.TestResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.UUID;

@Entity
@Immutable
@AttributeOverride(name = "result", column = @Column(nullable = false))
public class TestEvent extends BaseTestInfo {
	private static final Logger LOG = LoggerFactory.getLogger(TestEvent.class);

	@Column
	@Type(type = "jsonb")
	private Person patientData;

	@Column
	@Type(type = "jsonb")
	private Provider providerData;

	@Column
	@Type(type = "jsonb")
	private AskOnEntrySurvey patientAnswersData;

	@ManyToOne(optional = false)
	@JoinColumn(name="test_order_id")
	private TestOrder order;

	@Column(columnDefinition = "uuid")
	private UUID priorCorrectedTestEventId;	// used to chain events

	public TestEvent() {}

	public TestEvent(TestResult result, DeviceType deviceType, Person patient, Facility facility, TestOrder order) {
		super(patient, facility, deviceType, result);
		// store a link, and *also* store the object as JSON
		this.patientData = getPatient();
		this.providerData = getFacility().getOrderingProvider();
		this.order = order;
		super.setDateTestedBackdate(order.getDateTestedBackdate());
		PatientAnswers answers = order.getAskOnEntrySurvey();
		if (answers != null) {
			this.patientAnswersData = order.getAskOnEntrySurvey().getSurvey();
		} else {
			// this can happen during unit tests, but never in prod.
			LOG.error("Order {} missing PatientAnswers", order.getInternalId());
		}
	}

	public TestEvent(TestOrder order) {
		this(order.getResult(), order.getDeviceType(), order.getPatient(), order.getFacility(), order);
	}

	// Constructor for creating corrections. Copy the original event
	public TestEvent(TestEvent event, TestCorrectionStatus correctionStatus, String reasonForCorrection) {
		super(event, correctionStatus, reasonForCorrection);

		this.order = event.getTestOrder();
		this.patientData = event.getPatient();
		this.providerData = event.getProviderData();
		this.order = event.getTestOrder();
		// this.patient_answers_data =
		this.patientAnswersData = event.getPatientAnswersData();
		super.setDateTestedBackdate(order.getDateTestedBackdate());
		this.priorCorrectedTestEventId = event.getInternalId();
	}

	public Person getPatientData() {
		return patientData;
	}

	public AskOnEntrySurvey getPatientAnswersData() {
		return patientAnswersData;
	}

	public Date getDateTested() {
		if ( getDateTestedBackdate() != null) {
			return getDateTestedBackdate();
		} else {
			return getCreatedAt();
		}
	}

	public Provider getProviderData() {
		return providerData;
	}

	public TestOrder getTestOrder() {
		return order;
	}

	public UUID getTestOrderId() { return order.getInternalId(); }

	public UUID getPriorCorrectedTestEventId() {
		return priorCorrectedTestEventId;
	}

}
