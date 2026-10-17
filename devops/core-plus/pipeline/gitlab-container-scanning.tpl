{{- /* Aqua Security Trivy v0.76.0 contrib/gitlab.tpl, Apache-2.0.
Source: https://github.com/aquasecurity/trivy/blob/v0.76.0/contrib/gitlab.tpl */ -}}
{
  "version": "15.0.7",
  "scan": {
    "analyzer": {
      "id": "trivy",
      "name": "Trivy",
      "vendor": {
        "name": "Aqua Security"
      },
      "version": "{{ appVersion }}"
    },
    "end_time": "{{ now | date "2006-01-02T15:04:05" }}",
    "scanner": {
      "id": "trivy",
      "name": "Trivy",
      "url": "https://github.com/aquasecurity/trivy/",
      "vendor": {
        "name": "Aqua Security"
      },
      "version": "{{ appVersion }}"
    },
    "start_time": "{{ now | date "2006-01-02T15:04:05" }}",
    "status": "success",
    "type": "container_scanning"
  },
  {{- $image := "Unknown" -}}
  {{- $os := "Unknown" -}}
  {{- range . }}
    {{- if eq .Class "os-pkgs" -}}
      {{- $target := .Target }}
        {{- $image = $target | regexFind "[^\\s]+" }}
        {{- $os = $target | splitList "(" | last | trimSuffix ")" }}
    {{- end }}
  {{- end }}
  "vulnerabilities": [
  {{- $t_first := true }}
  {{- range . }}
    {{- range .Vulnerabilities -}}
    {{- if $t_first -}}
      {{- $t_first = false -}}
    {{ else -}}
      ,
    {{- end }}
    {
      "id": "{{ .VulnerabilityID }}",
      "name": {{ .Title | printf "%q" }},
      "description": {{ .Description | printf "%q" }},
      "severity": {{ if eq .Severity "UNKNOWN" -}}
                    "Unknown"
                  {{- else if eq .Severity "LOW" -}}
                    "Low"
                  {{- else if eq .Severity "MEDIUM" -}}
                    "Medium"
                  {{- else if eq .Severity "HIGH" -}}
                    "High"
                  {{- else if eq .Severity "CRITICAL" -}}
                    "Critical"
                  {{-  else -}}
                    "{{ .Severity }}"
                  {{- end }},
      "solution": {{ if .FixedVersion -}}
                    "Upgrade {{ .PkgName }} to {{ .FixedVersion }}"
                  {{- else -}}
                    "No solution provided"
                  {{- end }},
      "location": {
        "dependency": {
          "package": {
            "name": "{{ .PkgName }}"
          },
          "version": "{{ .InstalledVersion }}"
        },
        "operating_system": "{{ $os }}",
        "image": "{{ $image }}"
      },
      "identifiers": [
        {
          "type": "cve",
          "name": "{{ .VulnerabilityID }}",
          "value": "{{ .VulnerabilityID }}"
          {{- if .PrimaryURL | regexMatch "^(https?|ftp)://.+" -}},
          "url": "{{ .PrimaryURL }}"
          {{- end }}
        }
      ],
      "links": [
        {{- $links := list -}}
        {{- range .References -}}
          {{- if . | regexMatch "^(https?|ftp)://.+" -}}
            {{- $links = append $links . -}}
          {{- end -}}
        {{- end -}}
        {{- range $i, $url := $links }}
        {{- if $i }},{{ end }}
        {
          "url": "{{ $url }}"
        }
        {{- end }}
      ]
    }
    {{- end -}}
  {{- end }}
  ],
  "remediations": []
}
