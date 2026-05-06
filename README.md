# Mini Ride-Share Platform (Spring Boot + Kafka + K8s)

*This doc will be updated as I progress through implementation*


This project is a small ride-share style app I built to practice and demo that I can take something from idea to production-ish setup.

It covers the full flow: 

- writing backend services in Java/Spring Boot, 
- wiring async events with Kafka, 
- storing core data in MySQL, 
- using Redis for fast lookups, and 
- adding Python jobs for background processing/analytics.

Then I deploy everything on **Kubernetes** and manage releases the **GitOps** way with ArgoCD on a **Bare-Metal cluster**. Also, I will be adding Terraform for provisioning clusters in GCP and AWS to deploy this app. 

Hardware - R720 PowerEdge Server x 4 (From my **Homelab**)

The goal isn’t to build a full Uber clone - it’s to show practical engineering skills across:

- service design and API development
- event-driven architecture
- caching + database usage
- containerization and K8s deployment
- CI/CD + GitOps workflows
- basic scaling patterns (replicas, Kafka partitions, stateless services)

You can think of it as a “real-world-ish” demo project that proves I can design, code, ship, and scale a distributed system with a modern stack.

---
## Diagrams & Screenshots

![Architecture diagram](docs/images/arch-diagram.drawio.svg)

## Argo CD
![ArgoCD](docs/images/argo-cd.png)

## Github Actions
![Github Actions](docs/images/github-actions-build.png)

---

## Technical Details

*TODO: Update README as I progress through implementation*

### App will (probably) contain

- auth-gateway-service
- rider-service
- driver-service
- dispatch-engine
- location-tracker
- common-dto

### K8s related setup

- Enabled metallb for provisioning LBs
- Enabled microk8s dashboard
- Installed argocd
- brought in remote config from cluster host into laptop
- patched svc for k8s and argocd dashboards
  > kubectl patch svc kubernetes-dashboard-kong-proxy -n kubernetes-dashboard -p '{"spec": {"type": "LoadBalancer"}}'
  >
  > kubectl patch svc argocd-server -n kubernetes-dashboard -p '{"spec": {"type": "LoadBalancer"}}'


Things to automate
- Configure mysql password, jwt secret, dockerhub as secrets outside of git
- Creating ingress-controller(nginx) & Patch it to type LB
 > kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/controller-v1.10.1/deploy/static/provider/baremetal/deploy.yaml
 >
 > kubectl patch svc ingress-nginx-controller -n ingress-nginx -p '{"spec": {"type": "LoadBalancer"}}'

## Debugging

Notes on troubleshooting this setup: [Debugging](docs/Debugging.md).


## To Do

1. Move DTOs into a depedency model.
2. Move to a shared/overlayed Helm chart method since I have more services now.
3. Validating Admission Controllers
4. Create cursor skills for auto linting
5. Move Kafka to strimzi version.
6. Deploy an cache for images/dependencies etc.


Decisions :
1. Not using Spring security AuthenticationManagement for the MVP
2. Going with a shared secret on K8s for the jwt public key. Later move it into a JWKS endpoint on authservice and limit the public key to only one service. Need to think of trade offs there.
3. Not using an umbrella chart method and instead create a argo-cd app pointed at bitnami chart for Kafka. 
4. Not using a image cache like Nexus/Artifactory.
