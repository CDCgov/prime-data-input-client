import React from "react";
import classnames from "classnames";

import Required from "../commonComponents/Required";
import Optional from "../commonComponents/Optional";

import useUniqueIds from "./useUniqueIds";

// Checkbox objects need a value and label but also can have intrinsic `input`
// DOM properties such as `disabled`, `readonly`, `aria-xxx` etc.
export type CheckboxProps = {
  value: string;
  label: string;
};
type InputProps = JSX.IntrinsicElements["input"];
type Checkbox = CheckboxProps & InputProps;
interface Props {
  boxes: Checkbox[];
  checkedValues?: { [key: string]: boolean | undefined };
  legend: React.ReactNode;
  legendSrOnly?: boolean;
  name: string;
  disabled?: boolean;
  className?: string;
  errorMessage?: string;
  validationStatus?: "error" | "success";
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  required?: boolean;
  inputRef?: React.RefObject<HTMLInputElement>;
}

const Checkboxes = (props: Props) => {
  const {
    boxes,
    name,
    legend,
    checkedValues = {},
    onChange,
    legendSrOnly,
    validationStatus,
    errorMessage,
    required,
    inputRef,
  } = props;
  const checkboxIds = useUniqueIds("check", boxes.length);

  return (
    <fieldset
      className={classnames(
        "usa-fieldset prime-checkboxes",
        validationStatus === "error" && "usa-form-group--error",
        props.className
      )}
    >
      <legend
        className={classnames(
          "usa-legend",
          validationStatus === "error" && "usa-label--error",
          legendSrOnly && "usa-sr-only"
        )}
      >
        {required ? <Required label={legend} /> : <Optional label={legend} />}
      </legend>
      {validationStatus === "error" && (
        <div className="usa-error-message" role="alert">
          <span className="usa-sr-only">Error: </span>
          {errorMessage}
        </div>
      )}
      <div
        className={classnames(
          "usa-form-group",
          validationStatus === "error" && "usa-form-group--error"
        )}
      >
        {boxes.map(({ value, label, disabled, checked, ...inputProps }, i) => (
          <div className="usa-checkbox" key={checkboxIds[i]}>
            <input
              className="usa-checkbox__input"
              checked={checked || checkedValues?.[value] || false}
              id={checkboxIds[i]}
              onChange={onChange}
              type="checkbox"
              value={value}
              name={name}
              ref={inputRef}
              disabled={disabled || props.disabled}
              onFocus={(e) => {
                let target = e.target;
                target.scrollIntoView();
                if(target){
                  let topPos = target.offsetTop;
                  let modal = target.closest('.ReactModal__Content');
                  console.log(modal);
                  if(modal)
                    modal.scrollTop -= 10;
                  console.log(topPos);
                }
                console.log(e.target);
              }}
              {...inputProps}
            />
            <label className="usa-checkbox__label" htmlFor={checkboxIds[i]}>
              {label}
            </label>
          </div>
        ))}
      </div>
    </fieldset>
  );
};

export default Checkboxes;
