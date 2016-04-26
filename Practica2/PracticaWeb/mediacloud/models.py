from __future__ import unicode_literals

from django.db import models


class Item(models.Model):
    description = models.CharField(max_length=400)
    name = models.CharField(max_length=40)
    type = models.CharField(max_length=10)
    price = models.FloatField(default=0)

    def __str__(self):
        return "[" + self.type + "] " + self.name


class Types(models.Model):
    name = models.CharField(max_length=400)

    def __str__(self):
        return "" + self.name
