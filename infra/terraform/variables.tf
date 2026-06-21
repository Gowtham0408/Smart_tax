variable "aws_region" {
  description = "AWS region for SmartTax resources."
  type        = string
  default     = "ap-south-1"
}

variable "documents_bucket_name" {
  description = "Globally unique S3 bucket name for uploaded tax documents."
  type        = string
}
