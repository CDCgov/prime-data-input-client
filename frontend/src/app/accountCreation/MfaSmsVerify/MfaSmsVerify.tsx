import { MfaVerify } from "../MfaVerify/MfaVerify";

interface Props {
  phoneNumber: string;
}

export const MfaSmsVerify = (props: Props) => (
  <MfaVerify
    hint={
      <>
        We’ve sent a text message (SMS) to <b>{props.phoneNumber}</b>. It will
        expire in 10 minutes.
      </>
    }
  />
);
