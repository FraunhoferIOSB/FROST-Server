{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "frost-server.name" -}}
{{- default .Chart.Name .Values.name | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "frost-server.fullName" -}}
{{- $name := default .Chart.Name .Values.name -}}
{{- if .tier -}}
{{- printf "%s-%s-%s" .Release.Name $name .tier | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "frost-server.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Get the HTTP service API version.
*/}}
{{- define "frost-server.http.apiVersion" -}}
v1.0
{{- end -}}

{{/*
Get the HTTP serviceHost.
*/}}
{{- define "frost-server.http.serviceHost" -}}
  {{ if not .Values.frost.http.serviceHost | empty }}{{ .Values.frost.http.serviceHost }}{{else}}"frost-server"{{end}}
{{- end -}}

{{/*
Get the HTTP service root URL.
*/}}
{{- define "frost-server.http.serviceRootUrl" -}}
{{ .Values.frost.http.serviceProtocol }}://{{ .Values.frost.http.serviceHost }}{{ if .Values.frost.http.servicePort }}:{{ .Values.frost.http.servicePort }}{{ else if not .Values.frost.http.ingress.enabled }}:{{ .Values.frost.http.ports.http.nodePort }}{{ end }}{{ template "frost-server.http.serviceSubPath" . }}
{{- end -}}

{{/*
Get the HTTP service SubPath
*/}}
{{- define "frost-server.http.serviceSubPath" -}}
  {{- if not .Values.frost.http.urlSubPath | empty -}}
      {{- printf "/%s/" .Values.frost.http.urlSubPath | replace "//" "/" -}}
  {{- else -}}
      {{- printf "/" -}}
  {{- end -}}
{{- end -}}

{{/*
Get the MQTT serviceHost.
*/}}
{{- define "frost-server.mqtt.serviceHost" -}}
  {{ if not .Values.frost.mqtt.serviceHost | empty }}{{ .Values.frost.mqtt.serviceHost }}{{else}}{{ .Values.frost.http.serviceHost }}{{end}}
{{- end -}}

{{/*
Get the MQTT service root URL.
*/}}
{{- define "frost-server.mqtt.serviceRootUrl" -}}
{{ .Values.frost.mqtt.serviceProtocol }}://{{ template "frost-server.mqtt.serviceHost" . }}{{ if .Values.frost.mqtt.servicePort }}:{{ .Values.frost.mqtt.servicePort }}{{ else if not .Values.frost.mqtt.ingress.enabled }}:{{ .Values.frost.mqtt.ports.mqtt.nodePort }}{{ end }}{{ if .Values.frost.mqtt.ingress.enabled }}{{ template "frost-server.mqtt.websockPath" . }}{{ end }}
{{- end -}}

{{/*
Get the MQTT Websock-Path.
*/}}
{{- define "frost-server.mqtt.websockPath" -}}
  {{- if not .Values.frost.mqtt.urlSubPath | empty -}}
      {{- printf "/%s" .Values.frost.mqtt.urlSubPath | replace "//" "/" -}}
  {{- else -}}
      {{- printf "/mqtt" -}}
  {{- end -}}
{{- end -}}

{{/*
Get the MQTT TCP service EndPoint
*/}}
{{- define "frost-server.mqtt.serviceEndpoint" -}}
  {{- if and .Values.frost.http.serviceHost .Values.frost.mqtt.ports.mqtt.nodePort -}}
      {{- printf "%s:%s" .Values.frost.http.serviceHost .Values.frost.mqtt.ports.mqtt.nodePort -}}
  {{- else -}}
      {{- printf "NOT CONFIGURED -- please set frost.mqtt.ports.mqtt.nodePort in values.yaml" -}}
  {{- end -}}
{{- end -}}

{{/*
Get the default agic rewriteAnnotations for ingress.
*/}}
{{- define "frost-server.ingress.rewriteAnnotation" -}}
  {{- $myannotations := dict -}}  
  {{- if eq .scope.ingress.ingressProvider "agic" -}} {{/* Set annotations for ingress of type azure agic */}}
    {{- if .scope.ingress.tls.enabled -}}
      {{- $_ := set $myannotations "appgw.ingress.kubernetes.io/ssl-redirect" "true" -}}
    {{- end -}}
    {{- if eq .type "http" -}}
      {{- $_ := set $myannotations "appgw.ingress.kubernetes.io/backend-path-prefix" "/FROST-Server/" -}}
      {{/* put here default annotations for http-service */}}
    {{- else if eq .type "mqtt" -}}
      {{/* put here default annotations for mqtt-service */}}
    {{- end -}}
  {{- else if eq .scope.ingress.ingressProvider "traefik" -}} {{/* Set annotations for ingress of type traefik */}}
    {{- if .scope.ingress.tls.enabled -}}
      {{- $_ := set $myannotations "traefik.ingress.kubernetes.io/router.tls" "true" -}}
    {{- end -}}
    {{- if eq .type "http" -}}
      {{/* put here default annotations for http-service */}}
    {{- else if eq .type "mqtt" -}}
      {{/* put here default annotations for mqtt-service */}}
    {{- end -}}
  {{- else if eq .scope.ingress.ingressProvider "nginx" -}} {{/* Set annotations for ingress of type kubernetes.nginx */}}
    {{- if .scope.ingress.tls.enabled -}}
      {{- $_ := set $myannotations "nginx.ingress.kubernetes.io/ssl-redirect" "true" -}}
    {{- end -}}
    {{- if eq .type "http" -}}
      {{- $_ := set $myannotations "nginx.ingress.kubernetes.io/rewrite-target" "/FROST-Server/$1" -}}
      {{/* put here default annotations for http-service */}}
    {{- else if eq .type "mqtt" -}}
      {{- $_ := set $myannotations "nginx.mqtt.hamel.test" "true" -}}
      {{/* put here default annotations for mqtt-service */}}
    {{- end -}}
  {{- end -}}
  {{- $myannotations | toYaml -}}
{{- end -}}


{{/*
Get the DB secret.
*/}}
{{- define "frost-server.db.secret" -}}
  {{- if .Values.frost.db.existingSecret -}}
    {{- .Values.frost.db.existingSecret -}}
  {{- else -}}
    {{ include "frost-server.fullName" . }}
  {{- end -}}
{{- end -}}


