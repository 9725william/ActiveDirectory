/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.simple.portalreportes.controller;

import com.simple.portalreportes.pojos.UsuarioDA;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

/**
 *
 * @author jarguello
 */
public class ActiveDirectory {

    // set the LDAP authentication method
    private final String auth_method = "simple";
    // set the LDAP client Version
    private final String ldap_version = "3";
    // This is our LDAP Server's IP
    private final String ldap_host = "192.168.0.20";
    //private final String ldap_host = "localhost";
    // This is our LDAP Server's Port
    private final String ldap_port = "389";
    // This is our base DN
    private final String base_dn = "DC=SIMPLE,DC=local";
    // This is our access ID
    private String ldap_dn = "";
    // This is our access PW
    private String ldap_pw = "";

    public ActiveDirectory() {
    }

    public UsuarioDA getLdapContext(String username, String password) throws Exception {

        UsuarioDA usuario = new UsuarioDA();

        // This is our access ID
        this.ldap_dn = "SIMPLE\\" + username;
        // This is our access PW
        this.ldap_pw = password;

        try {

            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.SECURITY_AUTHENTICATION, this.auth_method);
            env.put(Context.SECURITY_PRINCIPAL, this.ldap_dn);
            env.put(Context.SECURITY_CREDENTIALS, this.ldap_pw);
            env.put(Context.PROVIDER_URL, "ldap://" + this.ldap_host + ":" + this.ldap_port);
            env.put("java.naming.ldap.version", this.ldap_version);
            LdapContext ctx = new InitialLdapContext(env, null);
            usuario = this.getUserBasicAttributes(username, ctx);

        } catch (NamingException nex) {
            Logger.getLogger(ActiveDirectory.class.getName()).log(Level.WARNING, "Usuario o contraseña incorrecta, Detalle error ", nex.getMessage());
        }
        return usuario;
    }

    private UsuarioDA getUserBasicAttributes(String username, LdapContext ctx) {
        UsuarioDA usuario = null;
        try {

            SearchControls constraints = new SearchControls();
            constraints.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String[] attrIDs = {"cn",
                "givenname",
                "sn",
                "sAMAccountName",
                "mail"};
            constraints.setReturningAttributes(attrIDs);
            //First input parameter is search bas, it can be "CN=Users,DC=YourDomain,DC=com"
            //Second Attribute can be uid=username
            NamingEnumeration answer = ctx.search(this.base_dn, "sAMAccountName=" + username, constraints);
            if (answer.hasMore()) {
                usuario = new UsuarioDA();
                Attributes attrs = ((SearchResult) answer.next()).getAttributes();
                usuario.setUsuario(attrs.get("sAMAccountName").get(0).toString());
                usuario.setNombre(attrs.get("givenname").get(0).toString());
                usuario.setApellido(attrs.get("sn").get(0).toString());
                usuario.setNombre_completo(attrs.get("cn").get(0).toString());
                usuario.setCorreo(attrs.get("mail").get(0).toString());
            } else {
                throw new Exception("Error en consulta de datos del usuario");
            }

        } catch (Exception ex) {
            //ex.printStackTrace();
        }
        return usuario;
    }

}
