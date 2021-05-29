import {
  removeTimer,
  TestTimerWidget,
  updateTimer,
  useTestTimer as originalUseTestTimer,
} from "../../app/testQueue/TestTimer";

const useTestTimer = (id: string, testLength: number) => {
  if (id === "completed-timer") {
    return {
      running: true,
      countdown: -10,
      elapsed: 100,
      start: () => {},
      reset: () => {},
    };
  }

  return originalUseTestTimer(id, testLength);
};

export { removeTimer, TestTimerWidget, updateTimer, useTestTimer };
