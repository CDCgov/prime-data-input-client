/* eslint no-unused-expressions: 0 */
function padSmallNumbers(num) {
  return num < 10 ? `0${num}` : num;
}

function verifyBirthDate(birthDate) {
  this.expect.section("@app").to.be.visible;
  this.expect
    .section("@app")
    .to.contain.text(
      "Enter your date of birth to access your COVID-19 Testing Portal."
    );
  this.section.app.expect.element("@dobInput").to.be.visible;
  const dobDate = new Date(birthDate);
  const formattedDob = `${padSmallNumbers(
    dobDate.getMonth() + 1
  )}/${padSmallNumbers(dobDate.getDate())}/${dobDate.getFullYear()}`;
  this.section.app.setValue("@dobInput", formattedDob);
  this.section.app.click("@dobSubmitButton");
  this.expect.section("@app").to.be.visible;
  this.expect.section("@app").to.contain.text("Profile information");
  return this;
}

function updateEmail(email) {
  // No email is generated by Faker, so we can add one here to assert edit flow works
  this.section.app.expect.element("@editPatientButton").to.be.visible;
  this.section.app.click("@editPatientButton");
  this.section.app.expect.element("@emailInput").to.be.visible;
  this.section.app.setValue("@emailInput", email);
  this.section.app.expect.element("@savePatientButton").to.be.visible;
  this.section.app.click("@savePatientButton");
  return this;
}

function verifyEmail(email) {
  this.section.app.expect.element("@patientEmail").to.be.visible;
  this.section.app.expect.element("@patientEmail").to.contain.text(email);
  this.section.app.expect.element("@patientConfirmButton").to.be.visible;
  this.section.app.click("@patientConfirmButton");
  this.expect
    .section("@app")
    .to.contain.text("Are you experiencing any of the following symptoms?");
  return this;
}

function completeQuestionnaire() {
  this.section.app.expect.element("@noSymptoms").to.be.visible;
  this.section.app.click("@noSymptoms");
  this.section.app.expect.element("@mostRecent").to.be.visible;
  this.section.app.click("@mostRecent");
  this.section.app.expect.element("@pregnant").to.be.visible;
  this.section.app.click("@pregnant");
  this.section.app.expect.element("@continueButton").to.be.visible;
  this.section.app.click("@continueButton");
}

module.exports = {
  url: (url) => url,
  commands: [
    {
      verifyBirthDate,
      updateEmail,
      verifyEmail,
      completeQuestionnaire,
    },
  ],
  sections: {
    app: {
      selector: ".App",
      elements: {
        dobInput: 'input[name="birthDate"]',
        dobSubmitButton: "#dob-submit-button",
        editPatientButton: "#edit-patient-profile-button",
        emailInput: 'input[name="email"]',
        savePatientButton: "#edit-patient-save-lower",
        noSymptoms: 'input[name="no_symptoms"][value="no"]+label',
        mostRecent: 'input[name="most_recent_flag"][value="yes"]+label',
        pregnant: 'input[name="pregnancy"][value="60001007"]+label',
        continueButton: "#aoe-form-save-button",
        patientEmail: "#patient-email",
        patientConfirmButton: "#patient-confirm-and-continue",
      },
    },
  },
};
