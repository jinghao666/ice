// **********************************************************************
//
// Copyright (c) 2003
// ZeroC, Inc.
// Billerica, MA, USA
//
// All Rights Reserved.
//
// Ice is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License version 2 as published by
// the Free Software Foundation.
//
// **********************************************************************

#ifndef ICE_XML_PARSER_H
#define ICE_XML_PARSER_H

#include <IceUtil/Shared.h>
#include <IceUtil/Handle.h>
#include <IceUtil/Exception.h>

#include <vector>
#include <map>

namespace IceXML
{

class ParserException : public IceUtil::Exception
{
public:
    ParserException(const std::string&);
    ParserException(const char*, int, const std::string&);

    virtual std::string ice_name() const;
    virtual void ice_print(std::ostream&) const;
    virtual Exception* ice_clone() const;
    virtual void ice_throw() const;

private:
    std::string _reason;
};

class Node;
typedef IceUtil::Handle< Node > NodePtr;

typedef std::vector<NodePtr> NodeList;

class Element;
typedef IceUtil::Handle< Element > ElementPtr;

class Text;
typedef IceUtil::Handle< Text > TextPtr;

class Document;
typedef IceUtil::Handle< Document > DocumentPtr;

typedef std::map<std::string, std::string> Attributes;

class Node : public IceUtil::Shared
{
public:
    virtual ~Node();

    virtual NodePtr getParent() const;
    virtual std::string getName() const;
    virtual std::string getValue() const;
    virtual NodeList getChildren() const;
    virtual Attributes getAttributes() const;
    virtual std::string getAttribute(const std::string&) const;

    virtual bool addChild(const NodePtr&);

protected:
    Node(const NodePtr&, const std::string&, const std::string&);

    NodePtr _parent;
    std::string _name;
    std::string _value;
};

class Element : public Node
{
public:
    Element(const NodePtr&, const std::string&, const Attributes&);
    virtual ~Element();

    virtual NodeList getChildren() const;
    virtual Attributes getAttributes() const;
    virtual std::string getAttribute(const std::string&) const;

    virtual bool addChild(const NodePtr&);

private:
    NodeList _children;
    Attributes _attributes;
};

class Text : public Node
{
public:
    Text(const NodePtr&, const std::string&);
    virtual ~Text();
};

class Document : public Node
{
public:
    Document();
    virtual ~Document();

    virtual NodeList getChildren() const;

    virtual bool addChild(const NodePtr&);

private:
    NodeList _children;
};

class Handler
{
public:
    virtual ~Handler();

    virtual void startElement(const std::string&, const Attributes&) = 0;
    virtual void endElement(const std::string&) = 0;
    virtual void characters(const std::string&) = 0;
    virtual void error(const std::string&, int, int);
};

class Parser
{
public:
    static DocumentPtr parse(const std::string&);
    static DocumentPtr parse(std::istream&);

    static void parse(const std::string&, Handler&);
    static void parse(std::istream&, Handler&);
};

}

#endif
