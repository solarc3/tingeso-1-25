## Microservicios

Detalles:

- Coneccion entre microservicios exclusivamente mediante y hacia el api-gateway
- frontend tambien es un pod clusterip (solo coneccion interna), se uso tambien la redireccion desde el api-gateway, asi se evita tener 2 load balancers y 2 ips externas, toda logica de comunicacion se centraliza en el api-gateway, toda la config del api-gateway esta dentro del [configmap](https://github.com/solarc3/tingeso-1-25/blob/main/Evaluacion%202/helm-chart/templates/namespace-configmap.yaml) 
- Se hace ingress con la ip publica que tiene asignada el api-gateway, al ser load balancer puede esperar trafico externo
- Para construir las imagenes se utiliza google cloud build, este requiere un archivo especial, en cada microservicio existe un `cloudbuild.yaml`, que compila con recursos de google y luego pushea hacia el `Google Artifact Registry`
- En vez de usar dockerhub, se prefirio utilizar el `Artifact Registry` de google, solamente por curioso y probar cosas nuevas, requiere configurar la cuenta de servicio que tiene el cluster de kubernetes y darle permisos a esa cuenta tambien
- NO se uso OpenFeign, toda comunicacion se hace via `Rest Template`, [ejemplo](https://github.com/solarc3/tingeso-1-25/blob/main/Evaluacion%202/microservices/reservations-service/src/main/java/tingeso/reservationsservice/services/ReservaService.java#L58C1-L66C6), esta comunicacion es hacia el `api-gateway` y no directamente hacia otro microservicio
- En vez de usar un contenedor de Postgres, se decidio usar `Google Cloud SQL` y generar una instancia, para poder permitir comunicacion con los pods, se debe agregar la network a la misma VPC que tiene el cluster de Kubernetes, se utilizo este file para probar conectividad via curl y wget -qO-

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: network-debug
  labels:
    app: network-debug
spec:
  replicas: 1
  selector:
    matchLabels:
      app: network-debug
  template:
    metadata:
      labels:
        app: network-debug
    spec:
      containers:
      - name: netshoot
        image: nicolaka/netshoot:latest
        command: ["sleep", "infinity"]
        securityContext:
          runAsUser: 0
        resources:
          limits:
            cpu:    "200m"
            memory: "256Mi"
```

Viene con hartas cositas instalas justamente para esto, la idea es conectarse directamente al pod via `kubectl exec -it nombreDeployment -- /bin/sh`, se puede recuperar el nombreDeployment via `kubectl get pods` o `kubectl get deployments`, recordar si se esta usando namespace sino agregar `-n NAMESPACE` a los comandos.
- Dentro de este contenedor se puede hacer `curl -I http://EUREKA-REGISTRY-NAME:RANDOM_PORT` donde EUREKA-REGISTRY-NAME son los nombres que reporta euraka server de las cosas que tiene registradas dentro

## HelmChart

En vez de generar cada deployment a mano, se decidio por usar un helm chart global, para poder utilizar se debe crear un archivo llamado `values.yaml` dentro de `/helm-chart`.

`microservices.yaml` es donde se generar los microservicios para lo que pide la evaluacion, todos tienen server.port 0 (que era exigencia) y el puerto 8081 es unicamente para los actuators (algo que no se pide, es un extra que hice yo). Para revisar que los microservicios si esten con puerto random se puede ejecutar: `kubectl exec -it <eureka-pod-name> -- curl http://localhost:8761/eureka/apps` dentro del mismo pod de eureka-server para ver las cosas que estan registradas dentro, esto retorna un XML y ahi se puede ver el nombre, ip y puerto asingado a cosas que se hayan registrado al eureka.

``` 
ls microservices | grep service$ | xargs -I {} kubectl logs -n kartingrm deployment/{} | grep EurekaAutoServiceRegistration
``` 
Se debe ejecutar en la raiz de Evaluacion 2, se aprovecha de que los deployment se llaman igual que las carpetas, entonces puedo directamente hacer un for loop de nombres y ver la salida, en esa linea con grep me muestra los puertos randoms asignados. De igual forma se puede hacer manual haciendo un `kubectl logs deployment NOMBRE  | grep EurekaAutoServiceRegistration` por cada uno, `xargs` simplemente permite ejecutar varias veces un mismo comando.

Para aplicar cambios, se debe utilizar Helm:
```
helm upgrade --install kartingrm ./helm-chart \
-f helm-chart/values.yaml \
--create-namespace
```
cada cambio que se haga dentro de los yamls, Helm es inteligente y solo actualiza el `kind` que fue modificado. Si bien directamente no se guardan los archivos de deployment, se puede usar 
```
helm template --dry-run ./helm-chart \
  --namespace kartingrm \
  -f helm-chart/values.yaml
```
Esto como salida presenta todos los files generados en combinacion de todos los que se tengan en `values.yaml` (btw esto tampoco lo pedian lo hice por aprender nomas, seria lo mismo que los exportara a files y que luego los actualizara via `kubectl rollout restart deployment`)

`infraestructure.yaml` es el que despliega exclusivamente `eureka-server`, `api-gateway` y `config-server`, estos 3 microservicios no tenian el requisito de puerto. De igual forma tampoco se podria con puerto dinamico, ya que si algo se vuelve config client, se mata si no puede recuperar su config.


## Config-Server

En vez de utilizar el git para tener un config-data aca y tener que subir un commit cada vez que se haga un cambio, se prefirio indagar y se encontro que se puede tener archivos locales como config data. Para lograr esto, dentro de [`helmchart/templates/namespace-configmap.yaml`](https://github.com/solarc3/tingeso-1-25/blob/main/Evaluacion%202/helm-chart/templates/namespace-configmap.yaml) el cual luego se monta dentro del contenedor de `config-server`:
```
        volumeMounts:
          - name: config-server-files
            mountPath: /etc/config

      volumes:
        - name: config-server-files
          configMap:
            name: config-server-files
```
En el [kind `Deployment` del config-server](https://github.com/solarc3/tingeso-1-25/blob/main/Evaluacion%202/helm-chart/templates/infraestructure.yaml#L103C1-L121C38) se puede apreciar lo que digo, entonces cada vez que se queria probar algo nuevo, simplemente se cambiaba, se hacia el `helm upgrade..` y luego `kubectl rollout restart deployment/config-server` y listo, ninguna necesidad de tener que recuperar y subir commits.


## Cloud Build

`Google Cloud Build` permite utilizar poder de computacion de google para compilas los `Dockerfile`, en este caso el `Dockerfile` de todos los microservicios es el mismo, el archivo que pide GCB es uno llamado `cloudbuild.yaml`, el cual tiene la siguiente forma:
```
steps:
- name: 'gcr.io/cloud-builders/docker'
  args:
    - 'build'
    - '-t'
    - '${_LOCATION}-docker.pkg.dev/$PROJECT_ID/${_REPOSITORY}/${_IMAGE}:${_TAG}'
    - '.'
- name: 'gcr.io/cloud-builders/docker'
  args:
    - 'push'
    - '${_LOCATION}-docker.pkg.dev/$PROJECT_ID/${_REPOSITORY}/${_IMAGE}:${_TAG}'

images:
  - '${_LOCATION}-docker.pkg.dev/$PROJECT_ID/${_REPOSITORY}/${_IMAGE}:${_TAG}'

substitutions:
  _LOCATION: 'southamerica-west1'
  _REPOSITORY: 'tingeso-registry'
  _IMAGE: 'customer-discounts-service'
  _TAG: 'latest'

timeout: '1200s'
```
Lo unico que hay que cambiar son los `substitutions`, si bien solo se tienen 4 subs, dentro de steps existen mas reemplazos, estos los ofrece directamente GCB y se hace el reemplazo de manera automatica, no es que falten, sino que GCB lo hace internamente, en los docs de GCB se habla de cuales son.

Para compilar a mano un unico `cloudbuild.yaml`, se puede mediante:
```
gcloud builds submit \
  --region=us-central1 \
  --config=cloudbuild.yaml \
  .
```
En este caso se hizo en la raiz de un microservicio, tuve inicialmente problemas con hacer builds en la region southamerica-west1 (ojo aca, es region, pq existen 3 espacios dentro de la region, yo siempre use southamerica-west1-b, que es una **ZONA** de la region), al parecer no se permite o tiene ciertos permisos, me pase a una americana por simplicidad, los primeros 2500 minutos de build son gratis

De igual forma, existe un `build-all.sh`, que permite compilar todos los microservicios a la vez y de forma paralela, aunque no usen GCB, se puede recuperar parte de script para poder construir imagenes en paralelo, imaginate en local, tambien se puede:
```
ls -1d */ \
  | xargs -n1 -P "$(ls -1d */ | wc -l)" -I % sh -c 'cd "%" && gcloud builds submit --region=us-central1 --config=cloudbuild.yaml .'

```
Este comando busca los microservicios (ls carpetas) y luego aplica un `xargs` con -P para paralelizar y todo lo que esta dentro de las comillas es para recuperar la cantidad de microservicios (todo para no escribir un 7, asi es dinamico) y el -I % permite llevar el resultado como argumento a cada ejecucion que se haga.

## Artifact Registry

El `Artifact Registry` es tener un espacio en la nube donde se pueden subir imagenes, la misma idea que dockerhub o harbor, solo se utilizo para probar, si se usara dockerhub u cualquier otro registry como ACS (Azure Container Registry), se debe cambiar dentro de cada template el valor de `image`, como ejemplo:

```
image: {{ $.Values.artifactRegistry.location }}-docker.pkg.dev/{{ $.Values.artifactRegistry.projectId }}/{{ $.Values.artifactRegistry.repository }}/{{ .name }}:{{ $.Values.imageTag }}
```

Todo lo que este alrededor de doble llaves se resuelve via el `values.yaml`, como ejemplo si tuviera esto en mi `values.yaml`:

```
artifactRegistry:
  location: southamerica-west1
  projectId: poetic-axle-XXXXX
  repository: tingeso-registry

namespace: kartingrm
imageTag: latest


microservices:
  - name: tariffs-service
    dbName: tariffs_service
    dbUser: tariffs_service_user
```
Estos microservicios se recuperarian dentro de los `kind: Deployment` como:
```
image: southamerica-west1-docker.pkg.dev/poetic-axle-XXXXX/tingeso-registry/tariffs-service:latest
```


