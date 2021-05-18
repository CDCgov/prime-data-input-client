// @ts-nocheck
import { useState, useEffect, useCallback } from "react";
import * as ZXing from "@zxing/library";

const codeReader = new ZXing.BrowserMultiFormatReader();

export type DecodedDataResult = {
  text: string;
  timestamp: Date;
  format: ZXing.BarcodeFormat;
};
export type DeviceItem = {
  deviceId: string;
  label: string;
};

const useBarcodeReader = (
  videoRef: React.RefObject<HTMLVideoElement>,
  onScan: (data: DecodedDataResult) => void
) => {
  const [streams, setStreams] = useState(null);
  const [devices, setDevices] = useState<Array<DeviceItem>>();
  const [device, setSelectedDevice] = useState<DeviceItem>();
  const [results, setResults] = useState<DecodedDataResult>();
  const [permissionError, setPermissionError] = useState<boolean>(false);
  const switchCamera = (newDevice) => {
    setSelectedDevice(newDevice);
  };

  const reset = () => {
    codeReader.reset();
  };

  const stopDecoding = useCallback(() => {
    reset();
    codeReader.stopAsyncDecode();
    codeReader.stopContinuousDecode();
  }, []);

  const handleScannedResult = useCallback(
    (result, err) => {
      if (result && result.text !== results?.text) {
        setResults(result);
        onScan(result);
        stopDecoding();
      }
      if (err && !(err instanceof ZXing.NotFoundException)) {
        console.error("[DECODE_ERROR]: ", err);
      }
    },
    [onScan, results?.text, stopDecoding]
  );

  const decode = useCallback(() => {
    if (device && videoRef.current) {
      console.log("[SCANNING]...");
      codeReader.decodeFromVideoDevice(
        device.deviceId,
        videoRef.current,
        handleScannedResult
      );
    }
  }, [device, videoRef, handleScannedResult]);
  const initMediaDevicesAccess = () => {
    console.log("[INIT]: Code reader start initialization");
    if (navigator && navigator.mediaDevices) {
      navigator.mediaDevices
        .getUserMedia({ audio: false, video: true })
        .then((stream) => {
          console.log("[SUCCESS]: Code reader init success");
          setStreams(stream.active);
          stream.getTracks().forEach(function (track) {
            track.stop();
          });
        })
        .catch((err) => {
          console.log("[STREAM_PERMISSION_ERROR]", err);
          setPermissionError(true);
        });
    }
  };
  useEffect(() => {
    initMediaDevicesAccess();
    return () => {
      stopDecoding();
    };
  }, [stopDecoding]);

  useEffect(() => {
    if (codeReader.canEnumerateDevices && codeReader.isMediaDevicesSuported) {
      codeReader.listVideoInputDevices().then((videoInputDevices) => {
        setDevices(videoInputDevices);
      });
    }
  }, [streams]);

  useEffect(() => {
    decode();
    // We need run decode only on deviceId has been changed.
    // eslint-disable-next-line
  }, [device?.deviceId]);

  useEffect(() => {
    if (devices?.length === 1) {
      switchCamera(devices[0]);
    }
  }, [devices]);

  return {
    codeReader,
    devices,
    switchCamera,
    currentDevice: device,
    decode,
    reset,
    stopDecoding,
    results,
    permissionError,
  };
};
export default useBarcodeReader;
