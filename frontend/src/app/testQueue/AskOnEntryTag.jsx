import React from "react";

export const areAnswersComplete = (answerDict) => {
  if (!answerDict.noSymptoms) {
    let symptomFound = false;
    const symptoms = JSON.parse(answerDict.symptoms);
    Object.values(symptoms).forEach((val) => {
      if (val) {
        symptomFound = true;
      }
    });
    if (!symptomFound) {
      return false;
    }
    if (answerDict.symptomOnset) {
      const onsetDate = answerDict.symptomOnset;
      if (
        onsetDate.year === "" ||
        onsetDate.month === "" ||
        onsetDate.day === ""
      ) {
        return false;
      }
    }
  }
  if (!answerDict.firstTest) {
    if (
      !answerDict.priorTestDate ||
      answerDict.priorTestDate.year === "" ||
      answerDict.priorTestDate.month === "" ||
      answerDict.priorTestDate.day === ""
    ) {
      return false;
    }
    if (!answerDict.priorTestType || !answerDict.priorTestResult) {
      return false;
    }
  }
  if (!answerDict.pregnancy) {
    return false;
  }
  return true;
};

const AskOnEntryTag = ({ aoeAnswers }) => {
  if (areAnswersComplete(aoeAnswers)) {
    return <span className="usa-tag bg-green">COMPLETED</span>;
  } else {
    return <span className="usa-tag">PENDING</span>;
  }
};

export default AskOnEntryTag;
