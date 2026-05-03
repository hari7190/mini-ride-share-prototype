## *This is going to be a messy file for a while. I promise to clean up later!*

#### Debug

For inspecting logs - especially ones crashing --previous helps to see the last logs
kubectl logs -n argocd -l app.kubernetes.io/name=argocd-applicationset-controller --previous

Delete a pod
kubectl delete pod -n argocd -l app.kubernetes.io/name=argocd-applicationset-controller


Problems:

1.  Argo CD ApplicationSet controller is failing

    Analysis using logs from my IDE- 
    Repeated error: failed to get restmapping: no matches for kind "ApplicationSet" in version "argoproj.io/v1alpha1" — the Kubernetes API server has no ApplicationSet resource (CRD not installed, wrong API version, or Argo CD install is incomplete/partial).
    Final failure: cache sync for *v1alpha1.ApplicationSet times out (~2 minutes of retries in that snippet), then the manager shuts down (problem running manager).

    harims@hms-mac ~ % kubectl apply -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/crds/applicationset-crd.yaml
    The CustomResourceDefinition "applicationsets.argoproj.io" is invalid: metadata.annotations: Too long: may not be more than 262144 bytes

    Adding a server side param to bypass the limit (for my bare-metal setup) "--server-side" seems to have pushed CRD creation.

2. auth-ingress still shows host ip! I want my nginx ingress ip there.
NAME           CLASS   HOSTS   ADDRESS         PORTS   AGE
auth-ingress   nginx   *       192.168.xx.xxx   80      10h
