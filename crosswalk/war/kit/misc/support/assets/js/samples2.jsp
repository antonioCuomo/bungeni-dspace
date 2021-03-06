<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/javascript" %>
<fmt:message key="welcome" var="welcome"/>
//<!-- 
var title= '${welcome}';
var tree_test = 1;
Ext.onReady(function(){	
	var catalog = [{
        title: 'Digital Repository (Grid)',
		iconCls:'icon-apps',
		cls:'active',
		treeid: 'treeid',
        samples: [{
            text: 'Feed Viewer 2.0',
            url: 'feed-viewer/view.html',
            icon: 'feeds.gif',
            desc: 'RSS 2.0 feed reader sample application that features a swappable reader panel layout.'
        }]
    },{
        title: 'Digital Repository (Icon)',
        iconCls:'icon-grid',
	treeid: '',
        samples: [{
            text: 'Basic Array Grid',
            url: 'grid/array-grid.html',
            icon: 'grid-array.gif',
            desc: 'A basic read-only grid loaded from local array data that demonstrates the use of custom column renderer functions.'
        }]
    },{
        title: 'Other Libraries',
        iconCls:'icon-tabs',
        treeid: '',
        samples: [{
            text: 'Basic Tabs',
            url: 'tabs/tabs.html',
            icon: 'tabs.gif',
            desc: 'Basic tab functionality including autoHeight, tabs from markup, Ajax loading and tab events.'
        }]
    }];

    for(var i = 0, c; c = catalog[i]; i++){
        c.id = 'sample-' + i;
    }

	var menu = Ext.get('sample-menu-inner'), 
		ct = Ext.get('sample-box-inner');
		
	var tpl = new Ext.XTemplate(
        '<div id="sample-ct">',
            '<tpl for=".">',
            '<div><a name="{id}" id="{id}"></a><h2><div unselectable="on">{title}</div></h2>',
            '<dl>',
                '<tpl for="samples">',
                    '<dd ext:url="{url}"><img title="{text}" src="/deploy/dev/examples/shared/screens/{icon}"/>',
                        '<div><h4>{text}</h4><p>{desc}</p></div>',
                    '</dd>',
                '</tpl>',
            '<div style="clear:left"></div></dl></div>',
            '</tpl>',
        '</div>'
    );

	/*tpl.overwrite(ct, catalog);*/
	
	
	var tpl2 = new Ext.XTemplate(
        '<tpl for="."><div><a href="#{id}" hidefocus="on" class="{cls}" id="a4{id}"><img src="s.gif" class="{iconCls}">{title}</a><div id="{treeid}"></div></div></tpl>'
    );
    tpl2.overwrite(menu, catalog);
    
    
    var splitter = Ext.get('treeid');
    splitter.createChild({id: 'tree0', tag: 'div', html: '', cls: ''/*, cn: {tag: 'code', style: '', name: '', type: '', size: 0}*/});
    splitter.createChild({id: 'tree1', tag: 'div', html: '', cls: ''});

	
	function calcScrollPosition(){
		var found = false, last;
		ct.select('a[name]', true).each(function(el){
			last = el;
			if(el.getOffsetsTo(ct)[1] > -5){
				activate(el.id)
				found = true;
				return false;
			}
		});
		if(!found){
			activate(last.id);
		}
	}
	
	var bound;
	function bindScroll(){
		ct.on('scroll', calcScrollPosition, ct, {buffer:250});
		bound = true;
	}
	function unbindScroll(){
		ct.un('scroll', calcScrollPosition, ct);
		bound = false;
	}
	function activate(id){
		Ext.get('a4' + id).radioClass('active');
	}
	
	ct.on('mouseover', function(e, t){
        if(t = e.getTarget('dd')){
            Ext.fly(t).addClass('over');
        }
    });
    ct.on('mouseout', function(e, t){
        if((t = e.getTarget('dd')) && !e.within(t, true)){
            Ext.fly(t).removeClass('over');
        }
    });
	ct.on('click', function(e, t){
        if((t = e.getTarget('dd', 5)) && !e.getTarget('a', 3)){
            var url = Ext.fly(t).getAttributeNS('ext', 'url');
			if(url){
				window.open(url.indexOf('http') === 0 ? url : ('/reports/frameset?__report=undesa.rptdesign&sample=my+parameter&__format=pdf&' + url +'='));
			}
        }else if(t = e.getTarget('h2', 3, true)){
			t.up('div').toggleClass('collapsed');
		}		
    });

	var /*addMember*/bd = Ext.get('sample-2');
// turn on validation errors beside the field globally
    Ext.form.Field.prototype.msgTarget = 'side';
/*
     * ================  Form 5  =======================
     */
    /*bd.createChild({tag: 'h2', html: 'Add Member'});

    var tab2 = new Ext.FormPanel({
        labelAlign: 'top',
        title: 'Add Member',
        bodyStyle:'padding:5px',
        width: 770,
        items: [{
            layout:'column',
            border:false,
            items:[{
                columnWidth:.5,
                layout: 'form',
                border:false,
                items: [{
                    xtype:'textfield',
                    fieldLabel: 'First Name',
                    name: 'first',
                    anchor:'95%'
                }, {
                    xtype:'textfield',
                    fieldLabel: 'Company',
                    name: 'company',
                    anchor:'95%'
                }]
            },{
                columnWidth:.5,
                layout: 'form',
                border:false,
                items: [{
                    xtype:'textfield',
                    fieldLabel: 'Last Name',
                    name: 'last',
                    anchor:'95%'
                },{
                    xtype:'textfield',
                    fieldLabel: 'Email',
                    name: 'email',
                    vtype:'email',
                    anchor:'95%'
                }]
            }]
        },{
            xtype:'tabpanel',
            plain:true,
            activeTab: 0,
            height:235,
            defaults:{bodyStyle:'padding:10px'},
            items:[{
                title:'Baptism Details',
                layout:'form',
                defaults: {width: 730},
                defaultType: 'textfield',

                items: [{
                    fieldLabel: 'Church Baptised',
                    name: 'first.name',
                    allowBlank:false,
                    value: 'Jack'
                },{
                    fieldLabel: 'Date Baptised',
                    name: 'last',
                    value: 'Slocum'
                }]
            },{
                title:'Contacts',
                layout:'form',
                defaults: {width: 730},
                defaultType: 'textfield',

                items: [{
                    fieldLabel: 'Home',
                    name: 'home',
                    value: '(888) 555-1212'
                },{
                    fieldLabel: 'Business',
                    name: 'business'
                },{
                    fieldLabel: 'Mobile',
                    name: 'mobile'
                },{
                    fieldLabel: 'Profession',
                    name: 'fax'
                }]
            },{
                cls:'x-plain',
                title:'Biography',
                layout:'fit',
                items: {
                    xtype:'htmleditor',
                    id:'bio2',
                    fieldLabel:'Biography'
                }
            }]
        }],

        buttons: [{
            text: 'Save'
        },{
            text: 'Cancel'
        }]
    });*/

    
    
    /*var bd2 = Ext.get('rere');
    var tabs = new Ext.TabPanel({
        renderTo: bd2, //Ext.getBody(),
        activeTab: 0,
        items: [{
            title: 'Tab 1',
            html: 'A simple tab'
        },{
            title: 'Tab 2',
            html: 'Another one'
        }]
    });*/
    
    
    
    
    
    
    
    //tab2.render(/*document.body*/bd);
////$('#sample-box-inner > div').hide();
    
	menu.on('click', function(e, t){
		console.info('at click');
		if((t = e.getTarget('a', 2))/* && bound*/){
			var ident = t.href.split('#')[1];
			/*var top = Ext.getDom(id).offsetTop;*/
			Ext.get(t).radioClass('active');
//Ext.get(ident).show();
			if(ident == 'sample-0'){
				$("#treeid").show();
			}else{
				$("#treeid").hide();
			}

			var cp = Ext.get("sample-box-inner");
		    var mgr = cp.getUpdateManager();
		    mgr.loadScripts=true;
		    mgr.update(ident+'.html');
/*$('#sample-box-inner > div').hide();
var test = '#'+ident;
//alert(ident + '     '+test);
$(test).show();*/
//Ext.get(t).enableDisplayMode('block');

//tab2.render(/*document.body*/bd);
//bd.radioClass('activity');
			/*unbindScroll();
			ct.scrollTo('top', top, {callback:bindScroll});*/

			e.stopEvent();
		}
	});
	
	
	
	var cp = Ext.get("sample-box-inner");
    var mgr = cp.getUpdateManager();
    mgr.loadScripts=true;
    mgr.update('sample-0'+'.html');
	
	
	
	
	
    
    
    /*ds.load({callback:function(success){
    	if(success){
    	alert('count:'+ds.getCount());
    	//alert(ds.getAt(0).get('question'));
    	ds.each(function(r){
    	//alert(r.get('employees')+' count:'+r.get('employees').length);
    	emp_arr = r.get('employees');
    	for(i=0; i < emp_arr.length; i++){
    	alert(emp_arr[i]['firstname']);
    	}
    	});*/
    
    
    
    
    /*var cp = Ext.get("sample-box-inner");
    var mgr = cp.getUpdateManager();
    mgr.loadScripts=true;
    mgr.update("grid.html");*/
    
    /*Ext.get("sample-box-inner").load(
            {
                url:"index.html",
                text:"loading",
                scripts:true
            }
        );*/
    
    
    
	/*var cp = Ext.get("sample-box-inner");
    var mgr = cp.getUpdateManager();
    mgr.loadScripts=true;
    mgr.update('sample-0'+'.html');*/
	
	
	
	
	Ext.get('samples-cb').on('click', function(e){
		var img = e.getTarget('img', 2);
		if(img){
			Ext.getDom('samples').className = img.className;
			calcScrollPosition.defer(10);
		}
	});
	
	bindScroll();
});
//-->