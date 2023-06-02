/*!
*    storagePlus.js   
*    (c) 2019/1/3 BkHumor       
*/
;(function(root, factory) {
        
        if(typeof define === 'function' && define.amd) {
                define('storagePlus', factory);
        } else if(typeof exports === 'object') {
                exports = module.exports = factory();
        } else {
                root.storagePlus = factory();
        }

})( typeof window != "undefined" ? window : this, function() {

        'use strict';
        var _storagePlus;
        _storagePlus = (function () {

                if(!localStorage) {throw new TypeError('Sorry! Your browser does not support it.');}
                var storagePlus = {
                        set    : s,
                        get    : g,
                        remove : r,
                        clear  : c
                };
                function s(k, v, t) {
                        try {

                                t = (!t || isNaN(t)) ? 60 : t;
                                var e = (new Date() - 1) + t * 1000;
                                var r = {
                                        val: v,
                                        exp: e
                                };
                                localStorage.setItem(k, JSON.stringify(r));
                        } catch (e) {
                                throw e;
                        }
                }
                function g(k) {
                        try {

                                var v = localStorage.getItem(k);
                                var r = JSON.parse(v);
                                var n = new Date() - 1;
                                if (!r) return null;
                                if (n > r.exp) {
                                        this.remove(k);
                                        return "";
                                }
                                return r.val;
                        } catch (e) {
                                throw e;
                                this.remove(k);
                                return null;
                        }
                }
                function r(k) {
                        localStorage.removeItem(k);
                }
                function c() {
                        
                        localStorage.clear();
                }
        
                return storagePlus;
        })();

        return _storagePlus;

})

