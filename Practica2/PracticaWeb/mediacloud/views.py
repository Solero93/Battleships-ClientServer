from django.contrib import auth
from django.http import HttpResponseRedirect, HttpResponse
from django.core.urlresolvers import reverse
from django.shortcuts import render

from mediacloud.models import Item, Types, comment, cart
from django import forms
from django.contrib.auth.forms import UserCreationForm
from django.http import HttpResponseRedirect
from django.shortcuts import render

def register(request):
    if request.method == 'POST':
        form = UserCreationForm(request.POST)
        if form.is_valid():
            new_user = form.save()
            return HttpResponseRedirect("/mediacloud")
    else:
        form = UserCreationForm()
    return render(request, "registration/register.html", {
        'form': form,
    })



def index(request):
    # TODO Hacer tabla de types
    catalog_by_type = Types.objects.all()
    context = {
        'types': catalog_by_type,
    }
    return render(request, 'index.html', context)


def catalog(request, type="all"):
    catalog_by_type = Item.objects.filter(type=type) if type != "all" and type != "" else Item.objects.all()
    context = {
        'catalog': catalog_by_type,
        'type': type,
    }
    return render(request, 'catalog.html', context)


def detall(request, id):
    item_by_id = Item.objects.get(pk=id)
    comments_by_id = comment.objects.filter(idItem=id)
    context = {
        'item': item_by_id,
        'comments': comments_by_id
    }
    return render(request, 'description.html', context)


def error(request):
    context = {
    }
    return render(request, 'error.html', context)


def buy(request):
    items=[]
    for id in request.session["selectedItems"] :
        items.append(Item.objects.get(pk=id))
    context = {
        'items': items
    }
    return render(request, 'buy.html', context)

def bought(request):

    request.session["bougthItems"] = request.session["selectedItems"]
    return HttpResponseRedirect(reverse('download'))

def download(request):
    items=[]
    for id in request.session["selectedItems"]:
        items.append(Item.objects.get(pk=id))
    context = {
        'items': items
    }
    return render(request, 'download.html', context)
def downloadFile(request, id):
    file="mediacloud/downloads/algo.mp3"
    fsock = open(file)
    response = HttpResponse(fsock, content_type ='audio/mpeg')
    response['Content-Disposition'] = "attachment; filename=" + str(file)
    return response

def shoppingcart(request):
    selectedItems = []
    for key in request.POST:
        if key.startswith("checkbox"):
            selectedItems.append(request.POST[key])

    request.session["selectedItems"] = selectedItems
    return HttpResponseRedirect(reverse('buy'))

def success(request):
    return HttpResponseRedirect(reverse('index'))
