import { z } from "zod";

export const loginSchema = z.object({
  email: z.string().email("Enter a valid email address"),
  password: z.string().min(8, "Password must be at least 8 characters"),
});

export const signupSchema = z.object({
  email: z.string().email("Enter a valid email address"),
  password: z
    .string()
    .min(8, "Password must be at least 8 characters")
    .regex(/[A-Z]/, "Password must include an uppercase letter")
    .regex(/[0-9]/, "Password must include a number")
    .regex(/[^a-zA-Z0-9]/, "Password must include a special character"),
});

export const createWorkflowSchema = z.object({
  name: z.string().min(1, "Workflow name is required").max(100, "Name too long"),
  description: z.string().max(500, "Description too long").optional().default(""),
});

export const generateWorkflowSchema = z.object({
  prompt: z.string().min(10, "Prompt must be at least 10 characters").max(2000, "Prompt too long"),
});

export const generateAndSaveSchema = z.object({
  workflowName: z.string().min(1, "Workflow name is required").max(100, "Name too long"),
  prompt: z.string().min(10, "Prompt must be at least 10 characters").max(2000, "Prompt too long"),
});

export type LoginFormValues = z.infer<typeof loginSchema>;
export type SignupFormValues = z.infer<typeof signupSchema>;
export type CreateWorkflowFormValues = z.infer<typeof createWorkflowSchema>;
export type GenerateWorkflowFormValues = z.infer<typeof generateWorkflowSchema>;
export type GenerateAndSaveFormValues = z.infer<typeof generateAndSaveSchema>;
