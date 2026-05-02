TODO: Update README


This is the root of the mini ride-share app project. 

This will contain
  - auth-gateway-service
  - rider-service
  - driver-service
  - dispatch-engine
  - location-tracker
  - common-dto



K8s related setup

Enabled metallb for provisioning LBs
Enabled microk8s dashboard
Installed argocd
brought in remote config from cluster host into laptop

patched svc for k8s and argocd dashboards
    kubectl patch svc kubernetes-dashboard-kong-proxy -n kubernetes-dashboard -p '{"spec": {"type": "LoadBalancer"}}'
    kubectl patch svc argocd-server -n kubernetes-dashboard -p '{"spec": {"type": "LoadBalancer"}}'

    retrieved argocd-initial-admin-secret
