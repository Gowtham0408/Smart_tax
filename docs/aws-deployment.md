# AWS Deployment Checklist

## Target Architecture

- EC2 runs the Spring Boot API and serves the React build with Nginx.
- MySQL runs on RDS for production, or local MySQL for portfolio demos.
- S3 stores uploaded salary slips and Form 16 documents.
- S3 lifecycle rules move older documents to cheaper storage.
- Lambda reacts to document upload events and writes metadata or audit logs.
- GitHub Actions builds and deploys release artifacts.

## EC2 Setup

1. Launch an Ubuntu EC2 instance.
2. Install Java 17, Node.js 20, Nginx, Docker, and Docker Compose.
3. Configure security groups for ports `22`, `80`, `443`, and the backend port if needed.
4. Add environment variables for database, OCR, S3, and optional Ollama/local AI.
5. Run backend as a `systemd` service.

## S3 Lifecycle

Use the Terraform module under `infra/terraform` to create a private bucket with lifecycle transitions:

- Move objects to Standard-IA after 30 days.
- Expire temporary OCR uploads after 365 days.

## GitHub Actions Deployment Extension

The included CI workflow validates all services. For deployment, add repository secrets:

- `EC2_HOST`
- `EC2_USER`
- `EC2_SSH_KEY`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_REGION`
- `S3_BUCKET`

Then add a deploy job that copies `backend/target/*.jar`, `frontend/dist`, and service files to EC2.
