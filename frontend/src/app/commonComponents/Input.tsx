import React, { useEffect, useState } from "react";

import TextInput, { HTMLInputElementType } from "./TextInput";

interface Props<T> {
  field: keyof T;
  formObject: T;
  label: string;
  className?: string;
  onChange: (field: keyof T) => (value: string) => void;
  validate: (field: keyof T) => Promise<void>;
  getValidationStatus: (name: keyof T) => "error" | undefined;
  errors: Partial<Record<keyof T, string>>;
  type?: HTMLInputElementType;
  required?: boolean;
  disabled?: boolean;
}

export const Input = <T extends { [key: string]: any }>({
  field,
  formObject,
  label,
  className,
  onChange,
  validate,
  getValidationStatus,
  errors,
  type,
  required,
  disabled,
}: Props<T>): React.ReactElement => {
  const onChangeHandler = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange(field)(e.target.value);
  };

  const [inputErrors, setErrors] = useState(errors);

  useEffect(() => {
    setErrors(errors);
  }, [errors]);

  return (
    <TextInput
      label={label}
      name={String(field)}
      value={formObject[field] || ""}
      onChange={onChangeHandler}
      onBlur={() => {
        validate(field);
      }}
      validationStatus={getValidationStatus(field)}
      errorMessage={inputErrors[field]}
      className={className}
      type={type}
      required={required}
      disabled={disabled}
    />
  );
};

export default Input;
