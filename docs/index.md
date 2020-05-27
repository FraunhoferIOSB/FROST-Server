---
# Feel free to add content and custom Front Matter to this file.
# To modify the layout, see https://jekyllrb.com/docs/themes/#overriding-theme-defaults
title: FROST Documentation
layout: default
category: main
order: 1
---

# FROST Documentation

These pages contain the documentation for FROST-Server.


{% assign mydocs = site.pages | sort: 'order' | group_by: 'category' %}
{% for category in site.data.categories %}
## {{ category.title }}
{% for catItem in mydocs %}
{% if catItem.name == category.key %}
{% assign items = catItem.items | sort: 'order' %}{% for item in items %}{% if item.title %}
* [{{ item.title }}]({{ site.baseurl }}{{ item.url }}) {% if item.childCategory %}{% for subcatItem in mydocs %}{% if subcatItem.name == item.childCategory %}{% assign subitems = subcatItem.items | sort: 'order' %}{% for subitem in subitems %}
  * [{{ subitem.title }}]({{ site.baseurl }}{{ subitem.url }}){% endfor %}{%endif%}{% endfor %}{%endif%}{%endif%}{% endfor %}
{%endif%}{% endfor %}

{% endfor %}


