let patientName, birthDate;


const getDobFormat = (browser) => {
  /* NOTE: DOB format is currently browser specific
       - Firefox does not support <input type='date' /> and takes the format accepted by the app
       - Chrome does support the date type and expects MM/DD/YYYY
  */
  return browser.capabilities.browserName === 'chrome' ? 'MM/DD/YYYY' : 'YYYY-MM-DD';
}

module.exports = {
  // '@disabled': true,
  '1. Add a patient': (browser) => {
    ({
      patientName,
      birthDate,
    } = browser.page.patients().navigate().addPatient(getDobFormat(browser)));

    console.info(`Adding patient ${patientName}...`);
  },
  '2. Conduct a test': (browser) => {
    console.info(`Conducting test for ${patientName}...`);
    browser.page.home().navigate().conductTest(patientName);
  },
  '3. Conduct a test through patient experience': (browser) => {
    console.info(`Conducting patient experience test for ${patientName}...`);
    const patientLinkPromise = browser.page
      .home()
      .navigate()
      .getPatientLink(patientName);
    patientLinkPromise.then((patientLink) => {
      browser.page
        .patientExperience()
        .navigate(patientLink)
        .acceptTos()
        .verifyBirthDate(birthDate)
        .updateEmail('foo@bar.com')
        .navigate(patientLink)
        .acceptTos()
        .verifyBirthDate(birthDate)
        .verifyEmail('foo@bar.com')
        .completeQuestionnaire();

      browser.page.home().navigate().verifyQuestionnaireCompleted(patientName);
    });
  },
};
