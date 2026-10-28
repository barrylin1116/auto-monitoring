import * as Yup from 'yup';


export function useYupValidator<T extends Yup.InferType<Yup.ObjectSchema<any>>>(
  schema?: Yup.ObjectSchema<any>
) {
  const validateField = async (field: keyof T, value: any, allValues?: T) => {
    if (!schema) return undefined;

    try {
      await schema.validateAt(field as string, allValues || { [field]: value });
      return undefined;
    } catch (err: any) {
      return err.message as string;
    }
  };

  const validateForm = async (values: T) => {
    if (!schema) return { isValid: true, errors: {} };

    try {
      await schema.validate(values, { abortEarly: false });
      return { isValid: true, errors: {} };
    } catch (err: any) {
      const errors: Record<string, string> = {};
      err.inner.forEach((e: any) => {
        errors[e.path] = e.message;
      });
      return { isValid: false, errors };
    }
  };

  return { validateField, validateForm };
}