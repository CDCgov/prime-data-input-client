import { FC } from "react";
import cls from "classnames";

import { ReactComponent as LogoMark } from "../../../img/simplereport-logomark-color.svg";

import "./Loading.scss";

interface Props {
  message?: string | undefined;
  className?: string;
}

const Loading: FC<Props> = ({ message, className }) => (
  <div className={cls(className, "logo-container flex-align-center padding-5")}>
    <LogoMark className="logo-container__image" />
    <div className="logo-container__message">{message}</div>
  </div>
);

export default Loading;
