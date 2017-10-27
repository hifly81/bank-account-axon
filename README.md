# RHOAR-lab

#### Look at this stuff only if you attended the RHOAR course.

## RHSSO
RHSSO project

```bash
  $ oc -new-project <name of the OpenShift RHSSO project>

  $ oc create -f ocp/rhsso/rhsso-app-secret.json -n <name of the OpenShift RHSSO project>
  $ oc policy add-role-to-user view system:serviceaccount:<name of the OpenShift RHSSO project>:sso-service-account -n <name of the OpenShift RHSSO project>

  $ oc process -f ocp/rhsso/rhsso71-postgresql-persistent.yaml -p HTTPS_NAME=jboss -p HTTPS_PASSWORD=mykeystorepass -p SSO_ADMIN_USERNAME=admin -p SSO_ADMIN_PASSWORD=admin -p SSO_REALM=coolstore | oc create -n <name of the OpenShift RHSSO project> -f -

```

RHSSO Administration Console --> Login with admin/admin

## Coolstore Gateway Service
Microservice with Spring Boot and Camel - This service acts as a API Gateway

```bash
  $ oc new-project <name of the OpenShift coolstore gateway project>

  $ export GATEWAY_PRJ=<name of the OpenShift coolstore catalog project>
  $ oc policy add-role-to-user view -z default -n $GATEWAY_PRJ

  $ oc create configmap coolstore-gateway --from-file=etc/application.properties -n $GATEWAY_PRJ

  $ mvn clean fabric8:deploy -Popenshift -Dfabric8.namespace=$GATEWAY_PRJ
```

## Inventory Service
Microservice with Wildfly Swarm - It is integrated with RHSSO

```bash
  $ oc new-project <name of the OpenShift coolstore inventory project>

  $ export INVENTORY_PRJ=<name of the OpenShift coolstore inventory project>
  $ oc process -f ocp/inventory-service/inventory-service-postgresql-persistent.yaml -p INVENTORY_DB_USERNAME=jboss -p INVENTORY_DB_PASSWORD=jboss -p INVENTORY_DB_NAME=inventorydb | oc create -f - -n $INVENTORY_PRJ

  $ oc create configmap app-config --from-file=project-defaults.yml -n $INVENTORY_PRJ

  $ mvn clean fabric8:deploy -Popenshift -Dfabric8.namespace=$INVENTORY_PRJ
```

## Cart Service
Microservice with Spring Boot

```bash
  $ oc new-project <name of the OpenShift coolstore cart project>

  $ export CART_PRJ=<name of the OpenShift coolstore cart project>

  $ oc policy add-role-to-user view -n $CART_PRJ -z default
  $ oc create configmap cart-service --from-literal=catalog.service.url=<catalog service url>

  $ mvn clean fabric8:deploy -Popenshift -Dfabric8.namespace=$CART_PRJ
```

## Catalog Service
Microservice with Vert.x

```bash
  $ oc new-project <name of the OpenShift coolstore catalog project>

  $ export CATALOG_PRJ=<name of the OpenShift coolstore catalog project>
  $ oc process -f ocp/catalog-service/coolstore-catalog-mongodb-persistent.yaml -p CATALOG_DB_USERNAME=mongo -p CATALOG_DB_PASSWORD=mongo -n $CATALOG_PRJ | oc create -f - -n $CATALOG_PRJ

  $ oc policy add-role-to-user view -z default -n $CATALOG_PRJ
  $ oc create configmap app-config --from-file=etc/app-config.yaml -n $CATALOG_PRJ

  $ mvn clean fabric8:deploy -Popenshift -Dfabric8.namespace=$CATALOG_PRJ
```
