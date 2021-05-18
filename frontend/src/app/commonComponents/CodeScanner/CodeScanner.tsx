import { useRef } from "react";

import Button from "../Button/Button";
import iconSprite from "../../../img/sprite.svg";

import useBarcodeReader, { DecodedDataResult } from "./hooks/useBarcodeReader";
import styles from "./CodeScanner.module.scss";

interface Props {
  onScan: (testId: string) => void;
}

const CodeScanner: React.FC<Props> = ({ onScan }) => {
  const videoRef = useRef<HTMLVideoElement>(null);

  const handleOnScan = (data: DecodedDataResult) => {
    onScan(data.text);
  };

  const {
    devices,
    currentDevice,
    switchCamera,
    permissionError,
  } = useBarcodeReader(videoRef, handleOnScan);

  const handleSwitchCamera = () => {
    if (devices) {
      if (currentDevice?.deviceId !== devices[0].deviceId) {
        switchCamera(devices[0]);
      } else {
        switchCamera(devices[1]);
      }
    }
  };

  return (
    <div className={styles.container}>
      {currentDevice?.deviceId && (
        <div className={styles.wrapper}>
          {devices && devices.length === 2 && (
            <>
              <Button
                onClick={() => {
                  handleSwitchCamera();
                }}
                variant={"unstyled"}
              >
                <svg
                  className="usa-icon text-white margin-left-neg-2px"
                  aria-hidden="true"
                  focusable="false"
                  role="img"
                >
                  <use xlinkHref={iconSprite + "#autorenew"}></use>
                </svg>
              </Button>
            </>
          )}
          <video
            id="video"
            style={{ border: "1px solid gray" }}
            ref={videoRef}
          />
        </div>
      )}
      {permissionError && (
        <div className="usa-alert usa-alert--error usa-alert--slim">
          <div className="usa-alert__body">
            <p className="usa-alert__text">
              You need allow using camera on this device
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default CodeScanner;
