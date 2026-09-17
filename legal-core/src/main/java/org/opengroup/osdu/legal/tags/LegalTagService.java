package org.opengroup.osdu.legal.tags;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import org.apache.http.HttpStatus;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.legal.LegalTag;
import org.opengroup.osdu.core.common.model.legal.ListLegalTagArgs;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTag;
import org.opengroup.osdu.core.common.model.legal.StatusChangedTags;
import org.opengroup.osdu.legal.jobs.LegalTagCompliance;
import org.opengroup.osdu.legal.logging.AuditLogger;
import org.opengroup.osdu.legal.FeatureFlagController;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagPublisher;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepository;
import org.opengroup.osdu.legal.provider.interfaces.ILegalTagRepositoryFactory;
import org.opengroup.osdu.legal.tags.dto.*;
import org.opengroup.osdu.legal.tags.util.PersistenceExceptionToAppExceptionMapper;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.opengroup.osdu.legal.Constants.LEGAL_QUERY_API_UNION_OPERATOR;
import static org.opengroup.osdu.legal.Constants.LEGAL_QUERY_API_INTERSECTION_OPERATOR;
import static org.opengroup.osdu.legal.Constants.LEGAL_QUERY_API_QUERY_SEPARATOR;
import static org.opengroup.osdu.legal.Constants.LEGAL_QUERY_API_ATTRIBUTE_SEPARATOR;
import static org.opengroup.osdu.legal.Constants.LEGAL_QUERY_API_BETWEEN_END;
import static org.opengroup.osdu.legal.Constants.LEGAL_QUERY_API_BETWEEN_START;
import static org.opengroup.osdu.legal.Constants.LEGAL_QUERY_API_ADD_OPERATOR;
import static org.opengroup.osdu.legal.Constants.LEGAL_QUERY_API_FREE_TEXT_ATTRIBUTE;

@Service
public class LegalTagService {

  PersistenceExceptionToAppExceptionMapper exceptionMapper;
  private final ILegalTagRepositoryFactory repositories;
  private final LegalTagConstraintValidator validator;
  private final AuditLogger auditLogger;
  private final ILegalTagPublisher legalTagPublisher;
  private final JaxRsDpsLog log;
  private final FeatureFlagController featureFlagController;

  @Autowired
  public LegalTagService(PersistenceExceptionToAppExceptionMapper exceptionMapper,
                         ILegalTagRepositoryFactory repositories,
                         LegalTagConstraintValidator validator,
                         AuditLogger auditLogger,
                         ILegalTagPublisher legalTagPublisher,
                         JaxRsDpsLog log,
                         FeatureFlagController featureFlagController) {
    this.exceptionMapper = exceptionMapper;
    this.repositories = repositories;
    this.validator = validator;
    this.auditLogger = auditLogger;
    this.legalTagPublisher = legalTagPublisher;
    this.log = log;
    this.featureFlagController = featureFlagController;
  }

  public LegalTagDto create(LegalTagDto legalTagDto, String tenantName) {
    if (legalTagDto == null)
      return null;
    validateExpirationDate(legalTagDto.getProperties().getExpirationDate());
    validator.isValidThrows(legalTagDto);

    ILegalTagRepository legalTagRepository = repositories.get(tenantName);

    LegalTag legalTag = LegalTagDto.convertFrom(legalTagDto);
    String prefix = tenantName + "-";
    if (!legalTag.getName().startsWith(prefix)) {
      legalTag.setName(prefix + legalTag.getName());
    }

    validator.isValidThrows(legalTag);
    legalTag.setDefaultId(); // set id based on final name
    legalTag.setIsValid(true);
    exceptionMapper.run(legalTagRepository::create, legalTag, "Error creating LegalTag.");

    auditLogger.createdLegalTagSuccess(singletonList(legalTag.toString()));

    return LegalTagDto.convertTo(legalTag);
  }

  public Boolean delete(
      String projectId, String name, DpsHeaders requestHeaders, String tenantName) {
    if (Strings.isNullOrEmpty(name) || requestHeaders == null)
      return false;

    LegalTag legalTag = getLegalTag(name, tenantName);
    if (legalTag == null)
      return true;
    ILegalTagRepository legalTagRepository = repositories.get(tenantName);
    Boolean result = exceptionMapper.run(legalTagRepository::delete, legalTag, "Error deleting LegalTag.");
    if (Boolean.TRUE.equals(result)) {
      publishMessageToPubSubOnDeletion(projectId, legalTag, requestHeaders);
      auditLogger.deletedLegalTagSuccess(singletonList(legalTag.toString()));
    }
    return result;
  }

  public LegalTagDto get(String name, String tenantName) {
    if (Strings.isNullOrEmpty(name))
      return null;

    LegalTagDtos tags = getBatch(new String[] { name }, tenantName);
    if (tags == null || tags.getLegalTags() == null || tags.getLegalTags().isEmpty())
      return null;
    else {
      auditLogger.readLegalTagSuccess(Collections.singletonList(name));
      return Iterables.get(tags.getLegalTags(), 0);
    }
  }

  public Collection<LegalTag> listLegalTag(boolean valid, String tenantName) {
    ILegalTagRepository legalTagRepository = repositories.get(tenantName);
    ListLegalTagArgs args = new ListLegalTagArgs();
    args.setIsValid(valid);
    return exceptionMapper.run(legalTagRepository::list, args, "Error retrieving LegalTag(s).");
  }

  public LegalTagDtos list(boolean valid, String tenantName) {
    Collection<LegalTag> tags = listLegalTag(valid, tenantName);
    LegalTagDtos outputs = legalTagsToReadableLegalTags(tags);
    List<String> names = outputs.getLegalTags().stream().map(x -> x.getName()).toList();
    auditLogger.readLegalTagSuccess(names);
    return outputs;
  }

  public LegalTagDtos getBatch(String[] names, String tenantName) {
    if (names == null)
      return null;

    Collection<LegalTag> legalTags = getLegalTags(names, tenantName);
    auditLogger.readLegalTagSuccess(Collections.singletonList(String.join(", ", names)));
    return legalTagsToReadableLegalTags(legalTags);
  }

  public InvalidTagsWithReason validate(String[] names, String tenantName) {
    List<InvalidTagWithReason> invalidTagsWithReason = new ArrayList<>();

    if (names == null || names.length == 0) {
      auditLogger.validateLegalTagSuccess();
      return new InvalidTagsWithReason(invalidTagsWithReason);
    }

    List<String> notFoundNames = new ArrayList<>(asList(names));

    Collection<LegalTag> legalTags = getLegalTags(names, tenantName);
    if (legalTags == null || legalTags.isEmpty()) {
      for (String name : names)
        generateInvalidTagsWithReason(invalidTagsWithReason, name, "LegalTag not found");
      return new InvalidTagsWithReason(invalidTagsWithReason);
    }

    for (LegalTag tag : legalTags) {
      notFoundNames.remove(tag.getName());
      String errors = validator.getErrors(tag);
      if (errors != null)
        generateInvalidTagsWithReason(invalidTagsWithReason, tag.getName(), errors);
    }

    if (!notFoundNames.isEmpty()) {
      for (String notFoundName : notFoundNames)
        generateInvalidTagsWithReason(invalidTagsWithReason, notFoundName, "LegalTag not found");
    }

    auditLogger.validateLegalTagSuccess();

    return new InvalidTagsWithReason(invalidTagsWithReason);
  }

  public LegalTagDto update(UpdateLegalTag newLegalTag, String tenantName) {
    if (newLegalTag == null)
      return null;
    validateExpirationDate(newLegalTag.getExpirationDate());
    LegalTag currentLegalTag = getLegalTag(newLegalTag.getName(), tenantName);
    if (currentLegalTag == null) {
      throw AppException.legalTagDoesNotExistError(newLegalTag.getName());
    }

    currentLegalTag.getProperties().setContractId(newLegalTag.getContractId());
    currentLegalTag.getProperties().setExpirationDate(newLegalTag.getExpirationDate());
    currentLegalTag.getProperties().setExtensionProperties(newLegalTag.getExtensionProperties());
    currentLegalTag.setDescription(newLegalTag.getDescription());
    validator.isValidThrows(currentLegalTag);

    auditLogger.updatedLegalTagSuccess(Collections.singletonList(currentLegalTag.toString()));

    return update(currentLegalTag, tenantName);
  }

  public LegalTagDto updateStatus(String legalTagName, Boolean isValid, String tenantName) {
    if (legalTagName == null)
      return null;

    LegalTag currentLegalTag = getLegalTag(legalTagName, tenantName);

    if (currentLegalTag == null)
      throw AppException.legalTagDoesNotExistError(legalTagName);

    currentLegalTag.setIsValid(isValid);
    return update(currentLegalTag, tenantName);
  }

  private LegalTagDto update(LegalTag currentLegalTag, String tenantName) {
    ILegalTagRepository legalTagRepository = repositories.get(tenantName);
    LegalTag output = exceptionMapper.run(legalTagRepository::update, currentLegalTag, "error");

    if (output == null)
      return null;
    auditLogger.updatedLegalTagSuccess(singletonList(currentLegalTag.toString()));
    return LegalTagDto.convertTo(output);
  }

  private LegalTagDtos legalTagsToReadableLegalTags(Collection<LegalTag> legalTags) {
    if (legalTags == null || legalTags.isEmpty())
      return new LegalTagDtos();

    List<LegalTagDto> convertedTags = new ArrayList<>();
    for (LegalTag tag : legalTags) {
      if (tag == null) {
        continue;
      }
      convertedTags.add(LegalTagDto.convertTo(tag));
    }
    LegalTagDtos output = new LegalTagDtos();
    output.setLegalTags(convertedTags);
    return output;
  }

  private Collection<LegalTag> getLegalTags(String[] names, String tenantName) {
    long[] ids = new long[names.length];
    String prefix = tenantName + "-";
    for (int i = 0; i < ids.length; i++) {
      var legalTag = names[i];
      if (!legalTag.startsWith(prefix)) {
        legalTag = prefix + legalTag;
      }
      ids[i] = LegalTag.getDefaultId(legalTag);
    }
    ILegalTagRepository legalTagRepository = repositories.get(tenantName);
    return exceptionMapper.run(legalTagRepository::get, ids, "Error retrieving LegalTag(s).");
  }

  private LegalTag getLegalTag(String name, String tenantName) {
    Collection<LegalTag> output = getLegalTags(new String[] { name }, tenantName);
    return output == null || output.isEmpty() ? null : Iterables.get(output, 0);
  }

  private void generateInvalidTagsWithReason(
      List<InvalidTagWithReason> invalidTagsWithReason, String name, String reason) {
    invalidTagsWithReason.add(new InvalidTagWithReason(name, reason));
  }

  private void publishMessageToPubSubOnDeletion(
      String projectId, LegalTag legalTag, DpsHeaders headers) {
    StatusChangedTags statusChangedTags = new StatusChangedTags();
    statusChangedTags
        .getStatusChangedTags()
        .add(new StatusChangedTag(legalTag.getName(), LegalTagCompliance.incompliant));
    try {
      legalTagPublisher.publish(projectId, headers, statusChangedTags);
    } catch (Exception e) {
      log.error("Error when publishing legaltag status change to pubsub", e);
    }
  }

  public LegalTagDtos queryLegalTag(QueryLegalTag searchQuery, boolean valid, String tenantName) {
    Collection<LegalTag> legalTags = listLegalTag(valid, tenantName);

    log.debug(String.format("DEBUG queryLegalTag Search query %s", searchQuery.toString()));
    log.debug(String.format("DEBUG Size of legal tags retrieved: %d", legalTags.size()));

    LegalTagDtos outputs;

    Collection<LegalTag> matchedTags = readInputAndSearch(searchQuery, legalTags);

    log.debug(String.format("DEBUG Number of legaltags matched with input criteria %d", matchedTags.size()));

    outputs = legalTagsToReadableLegalTags(matchedTags);
    List<String> names = outputs.getLegalTags().stream().map(x -> x.getName()).toList();
    auditLogger.readLegalTagSuccess(names);

    return outputs;
  }

  private Collection<LegalTag> readInputAndSearch(QueryLegalTag searchQuery, Collection<LegalTag> legalTags) {

    List<String> queryList = searchQuery.getQueryList();
    int limit = searchQuery.getLimit();
    List<String> operatorList = searchQuery.getOperatorList();
    boolean intersection = false;
    boolean union = true;
    String first;

    Collection<LegalTag> matchedTagList = null;
    ArrayList<Collection<LegalTag>> matchTagArrayList = new ArrayList<>();

    log.debug(String.format("DEBUG readInputAndSearch Search query %s %d", searchQuery.toString(), limit));

    String extractedSearchQuery = null;

    if (operatorList == null || operatorList.isEmpty()) {
      intersection = false;
      union = true;
      first = LEGAL_QUERY_API_UNION_OPERATOR;
      log.debug("empty operator");
    } else {
      first = operatorList.iterator().next();
      if (StringUtils.containsAnyIgnoreCase(first, LEGAL_QUERY_API_INTERSECTION_OPERATOR)) {
        intersection = true;
        union = false;
      } else if (StringUtils.containsAnyIgnoreCase(first, LEGAL_QUERY_API_UNION_OPERATOR)) {
        intersection = false;
        union = true;
      } else if (StringUtils.containsAnyIgnoreCase(first, LEGAL_QUERY_API_ADD_OPERATOR)) {
        intersection = false;
        union = false;
      } else {
        log.info(String.format("invalid operator %s", first));
        throw new AppException(HttpStatus.SC_BAD_REQUEST, "Error parsing operator list",
            "Error parsing operator list, expected intersection or union");
      }
    }

    ListIterator<String> queryListIt = queryList.listIterator();

    while (queryListIt.hasNext()) {

      extractedSearchQuery = queryListIt.next();
      Matcher m = Pattern.compile("\\[([^]]*)").matcher(extractedSearchQuery);
      while (m.find()) {

        extractedSearchQuery = m.group(1);
      }
      matchedTagList = parseInputAndSearch(extractedSearchQuery, legalTags);
      matchTagArrayList.add(matchedTagList);
    }

    if (union) {
      log.info(String.format("union %s", first));
      return matchTagArrayList.stream().flatMap(Collection::stream)
          .collect(Collectors.toMap(LegalTag::getName, Function.identity(), (existing, replacement) -> existing))
          .values();
    } else if (intersection) {

      List<LegalTag> allTags = matchTagArrayList.stream().flatMap(Collection::stream).toList();

      return allTags.stream()
          .collect(Collectors.groupingBy(LegalTag::getName))
          .entrySet().stream()
          .filter(entry -> entry.getValue().size() > 1)
          .flatMap(entry -> entry.getValue().stream().limit(1))
          .toList();

    } else { // return add
      return matchTagArrayList.stream().flatMap(Collection::stream).toList();
    }

  }

  private Collection<LegalTag> parseInputAndSearch(
      String searchQuery, Collection<LegalTag> legalTags) {

    Collection<LegalTag> matchedTags = new ArrayList<>();

    StringTokenizer st;
    String attribute = null;
    String pattern = null;
    String searchedString;
    String fromDate = null;
    String toDate = null;
    LocalDate fromlocalDate = null;
    LocalDate toLocalDate = null;
    boolean matchedTag = false;
    LocalDate expirationDate = null;

    Iterator<LegalTag> legaliterator = legalTags.iterator();
    log.debug(String.format("parseInputAndSearch Search query %s", searchQuery));

    if (searchQuery == null || searchQuery.equals("")) {
      log.error("Null query");
      throw new AppException(HttpStatus.SC_BAD_REQUEST, "Null Search Query", "Error Null Search Query");
    } else if (searchQuery.contains(LEGAL_QUERY_API_ATTRIBUTE_SEPARATOR)) {
      st = new StringTokenizer(searchQuery, LEGAL_QUERY_API_ATTRIBUTE_SEPARATOR);
      log.debug(String.format("DEBUG readInputAndSearch contains =, %s", searchQuery));
      int tokens = st.countTokens();
      if (tokens == 2) {
        attribute = st.nextToken().trim();
        pattern = st.nextToken().trim();
      } else {
        log.error("invalid query input %s", searchQuery);
        throw new AppException(HttpStatus.SC_BAD_REQUEST, "Error parsing attribute query",
            "Error parsing attribute query, expected attribute=string");
      }
    } else if (searchQuery.contains(LEGAL_QUERY_API_BETWEEN_START)) {
      // date
      log.debug(String.format("DEBUG readInputAndSearch contains (), %s", searchQuery));
      searchedString = searchQuery.substring(searchQuery.indexOf(LEGAL_QUERY_API_BETWEEN_START) + 1,
          searchQuery.lastIndexOf(LEGAL_QUERY_API_BETWEEN_END));

      if (searchedString.contains(LEGAL_QUERY_API_QUERY_SEPARATOR)) {
        st = new StringTokenizer(searchedString, LEGAL_QUERY_API_QUERY_SEPARATOR);
        fromDate = st.nextToken().trim();
        toDate = st.nextToken().trim();
      }

      fromlocalDate = LocalDate.parse(fromDate);
      toLocalDate = LocalDate.parse(toDate);

    } else {
      log.debug(String.format("DEBUG readInputAndSearch contains free text search, %s", searchQuery));
      attribute = LEGAL_QUERY_API_FREE_TEXT_ATTRIBUTE;
      pattern = searchQuery;
    }

    log.debug(String.format("DEBUG parseInputAndSearch pattern: %s attribute %s, %s", pattern, attribute, searchQuery));
    while (null != legaliterator && legaliterator.hasNext()) {
      LegalTag oneLegalTag = legaliterator.next();
      if (searchQuery.contains(LEGAL_QUERY_API_ATTRIBUTE_SEPARATOR)) {
        matchedTag = searchInLegalTag(attribute, pattern, oneLegalTag);
      } else if (searchQuery.contains(LEGAL_QUERY_API_BETWEEN_START)) {
        expirationDate = oneLegalTag.getProperties().getExpirationDate().toLocalDate();
        matchedTag = expirationDate.isAfter(fromlocalDate) && expirationDate.isBefore(toLocalDate);
      } else if (attribute.contains(LEGAL_QUERY_API_FREE_TEXT_ATTRIBUTE)) {
        matchedTag = searchInLegalTag(attribute, pattern, oneLegalTag);
      } else {
        log.error("Unexpected query state");
        throw new AppException(HttpStatus.SC_BAD_REQUEST, "Error processing query", "Error processing query");
      }

      if (matchedTag) {
        matchedTags.add(oneLegalTag);
      }
    }

    return matchedTags;
  }

  public boolean checkAttributeForMatch(String attribute,
      String pattern,
      LegalTag legalTag) {

    org.opengroup.osdu.core.common.model.legal.Properties findInProperties = legalTag.getProperties();
    BeanWrapper beanWrapper = new BeanWrapperImpl(findInProperties);

    log.debug("checkAttributeForMatch Attribute %s", attribute);
    switch (attribute) {
      case "countryOfOrigin" -> {
        List<String> countryList = findInProperties.getCountryOfOrigin();
        return countryList.stream().anyMatch(pattern::equalsIgnoreCase);
      }
      case "expirationDate" -> {
        Date expirationDate = findInProperties.getExpirationDate();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String strDate = dateFormat.format(expirationDate);
        if (StringUtils.contains(strDate, pattern)) {
          log.debug(String.format("expirationDate Match found: %s, %s", strDate, pattern));
          return true;
        }
        return false;
      }
      case LEGAL_QUERY_API_FREE_TEXT_ATTRIBUTE -> {
        log.debug(String.format("free text search: pattern : %s", pattern));
        return freeTextSearch(pattern, legalTag);
      }
      default -> {
        log.debug(String.format("Attribute from properties: %s", attribute));
        // An unknown attribute is a legitimate input: the caller may be searching the
        // extension properties, which searchInLegalTag falls back to when nothing matches.
        // Reading it without this guard raises NotReadablePropertyException, which surfaces
        // to the caller as HTTP 500 instead of an empty result.
        if (!beanWrapper.isReadableProperty(attribute)) {
          log.debug(String.format("Attribute is not a readable property: %s", attribute));
          return false;
        }
        Object value = beanWrapper.getPropertyValue(attribute);
        log.debug(String.format("Attribute from properties: %s, Value: %s", attribute, value));
        return (value instanceof String && StringUtils.containsAnyIgnoreCase(value.toString().trim(), pattern));
      }
    }
  }

  public boolean freeTextSearch(String pattern, LegalTag legalTag) {

    String name = legalTag.getName();
    if (StringUtils.containsAnyIgnoreCase(name.trim(), pattern)) {
      return true;
    }

    String description = legalTag.getDescription();
    if (StringUtils.containsAnyIgnoreCase(description.trim(), pattern)) {
      return true;
    }

    List<String> attributeList = new ArrayList<>();
    attributeList.add("countryOfOrigin");
    attributeList.add("contractId");
    attributeList.add("originator");

    if (Boolean.TRUE.equals(featureFlagController.isLegalTagQueryApiFreeTextAllFieldsFlagEnabled())) {
      attributeList.add("expirationDate");
      attributeList.add("dataType");
      attributeList.add("securityClassification");
      attributeList.add("personalData");
      attributeList.add("exportClassification");
    }

    org.opengroup.osdu.core.common.model.legal.Properties findInProperties = legalTag.getProperties();
    BeanWrapper beanWrapper = new BeanWrapperImpl(findInProperties);
    Object value;
    for (int i = 0; i < attributeList.size(); i++) {
      value = beanWrapper.getPropertyValue(attributeList.get(i));
      String stringValue = Objects.toString(value, "").trim();
      if (StringUtils.containsAnyIgnoreCase(stringValue, pattern)) {
        return true;
      }
    }

    return false;
  }

  public boolean searchInLegalTag(String attribute, String pattern, LegalTag legalTag) {

    BeanWrapper beanWrapper = new BeanWrapperImpl(legalTag);

    if (beanWrapper.isReadableProperty(attribute)) {
      log.debug(String.format("DEBUG searchInLegaltag isReadableProperty %s %s", attribute, pattern));

      final Object value = beanWrapper.getPropertyValue(attribute);
      return (value instanceof String && StringUtils.containsAnyIgnoreCase(value.toString(), pattern));

    } else {
      org.opengroup.osdu.core.common.model.legal.Properties findInProperties = legalTag.getProperties();
      beanWrapper = new BeanWrapperImpl(findInProperties);
      if (beanWrapper.isReadableProperty(attribute)) {

        log.debug(String.format("DEBUG searchInLegaltag calling checkAttributeForMatch %s=%s", attribute, pattern));
        return checkAttributeForMatch(attribute, pattern, legalTag);

      } else if (attribute.contains(LEGAL_QUERY_API_FREE_TEXT_ATTRIBUTE)) {
        log.debug(String.format("DEBUG searchInLegaltag calling checkAttributeForMatch %s=%s", attribute, pattern));
        boolean matchFound = checkAttributeForMatch(attribute, pattern, legalTag);
        if (!matchFound) {
          log.debug(String.format("DEBUG searchInLegaltag else extensionProperties %s %s", attribute, pattern));
          JSONObject jsonObject = new JSONObject(findInProperties.getExtensionProperties());
          return matchInExtensionPropertiesJSON(attribute, pattern, jsonObject);
        }
        return true;
      } else {
        log.debug(String.format("DEBUG searchInLegaltag else extensionProperties %s %s", attribute, pattern));
        JSONObject jsonObject = new JSONObject(findInProperties.getExtensionProperties());
        return matchInExtensionPropertiesJSON(attribute, pattern, jsonObject);
      }
    }
  }

  private boolean matchInExtensionPropertiesJSON(
      String attribute, String pattern, JSONObject jsonObject) {

    boolean didItMatch = false;
    boolean didItMatchKey = false;
    boolean didItMatchAny = false;

    String key = null;
    Object value = null;
    if (null != jsonObject && !didItMatch) {
      Iterator<String> jsonKeyIter = jsonObject.keySet().iterator();
      while (null != jsonKeyIter && jsonKeyIter.hasNext()) {
        key = jsonKeyIter.next();

        if (jsonObject.get(key) instanceof JSONObject nestedJson) {
          didItMatch = matchInExtensionPropertiesJSON(attribute, pattern, nestedJson);

        } else if (jsonObject.get(key) instanceof String) {
          value = jsonObject.get(key);
          didItMatchKey = key.equalsIgnoreCase(attribute)
              && StringUtils.containsAnyIgnoreCase(value.toString(), pattern);
          if (!didItMatchKey) {
            didItMatchAny = attribute.equalsIgnoreCase(LEGAL_QUERY_API_FREE_TEXT_ATTRIBUTE)
                && StringUtils.containsAnyIgnoreCase(value.toString(), pattern);
          }
          didItMatch = didItMatchKey || didItMatchAny;

        } else if (jsonObject.get(key) instanceof JSONArray jsonArray) {
          for (int i = 0; i < jsonArray.length(); i++) {
            if (jsonArray.get(i) instanceof JSONObject newJsonObj) {
              didItMatch = matchInExtensionPropertiesJSON(attribute, pattern, newJsonObj);
              if (didItMatch)
                break;
            }

            if (jsonArray.get(i) instanceof String) {
              value = jsonArray.get(i);
              didItMatchKey = key.equalsIgnoreCase(attribute)
                  && StringUtils.containsAnyIgnoreCase(value.toString(), pattern);
              if (!didItMatchKey) {
                log.debug(String.format("DEBUG Got %d Found %s=%s, Looking for %s=%s match: %b", i, key, value,
                    attribute, pattern, didItMatch));
                didItMatchAny = attribute.equalsIgnoreCase(LEGAL_QUERY_API_FREE_TEXT_ATTRIBUTE)
                    && StringUtils.containsAnyIgnoreCase(value.toString(), pattern);
              }
              didItMatch = didItMatchKey || didItMatchAny;
              if (didItMatch)
                break;
            }
          } // end for
        } else if (jsonObject.get(key) instanceof Boolean boolValue) {
          boolean boolPattern = Boolean.parseBoolean(pattern);
          didItMatch = key.equalsIgnoreCase(attribute) && (boolValue == boolPattern);
        }

        if (didItMatch)
          return true;
      }
    }
    return didItMatch;
  }
  private void  validateExpirationDate(Date expirationDate)
  {
    if(expirationDate != null) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
      String yearString = sdf.format(expirationDate);
      if (yearString.length() != 4) {
        throw new AppException(400, "BadRequest", String.format("Expiration Date has invalid year %s.", yearString));
      }
    }
  }
}