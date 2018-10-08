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
Get the HTTP service API version
*/}}
{{- define "frost-server.http.apiVersion" -}}
v1.0
{{- end -}}

{{/*
Get the HTTP service root URL
*/}}
{{- define "frost-server.http.serviceRootUrl" -}}
{{ .Values.frost.http.serviceProtocol }}://{{ .Values.frost.http.serviceHost }}{{ if .Values.frost.http.servicePort }}:{{ .Values.frost.http.servicePort }}{{ else if not .Values.frost.http.ingress.enabled }}:{{ .Values.frost.http.ports.http.nodePort }}{{ end }}{{ if .Values.frost.http.urlSubPath }}/{{ .Values.frost.http.urlSubPath }}{{ end }}
{{- end -}}
